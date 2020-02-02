'''Tool for generating the Java data model.'''

import io, os, re, sys

from contextlib import contextmanager
from operator import attrgetter

from AST import AstType, PrimitiveType, ClassType, UnionType, ListType
from gentools import ast_types_from_module, wordnum_sorted, get_disjunction_names, get_main_relation, get_primitive_attribute_relation, get_primitive_list_item_relation
from ASTExt import NodeType

class CodeWriter:
    '''Utility class for writing code, that maintains an indent level for strings
       submitted to it, and thus simplifies the process of outputting indented code to
       a file or stream.'''
    def __init__(self, out, indent_size=4):
        '''out         - Stream to which code shall be output
           indent_size - Number of characters by which to indent for each indentation level.'''
        self.out          = out         # Output stream
        self.indent_level = 0           # Current indentation level
        self.indent_size  = indent_size # Number of spaces per indentation level
        self.indent_next  = True        # Next input should be indented?

    def indent(self, levels=1):
        '''Increase (or decrease) the indentation level.'''
        self.indent_level += levels
        assert self.indent_level >= 0

    def dedent(self, levels=1):
        '''Decrease (or increase) the indentation level.'''
        self.indent(-levels)

    def write(self, code, indent=0):
        '''Write the given 'code' string. The string will be split by newlines, and the current
           indent prefixed (using spaces) before each line output. If the most recent write
           ended with a newline, then the first line of 'code' will be indented.
           
           code   - String of code to be written. NB: include a final newline unless the first
                    line of any subsequent write should _not_ be indented.
           indent - Optional adjustment to the current indentation level to adopt for this
                    particular write.
           '''
        if len(code) == 0:
            return
        self.indent(indent)
        # Split into lines, preserving newline characters
        lines = [ l + '\n' for l in code.split('\n') ]
        if not code.endswith('\n'):
            lines[-1] = lines[-1][:-1] # Last line is not terminated, so remove appended newline
        else:
            lines = lines[:-1] # Remove empty string appended to indicate final newline
        for line in lines:
            if self.indent_next:
                if line != '\n':
                    self.out.write(('%%%ds' % (self.indent_level * self.indent_size)) % '')
            self.out.write(line)
            self.indent_next = len(line) > 0 and line[-1] == '\n'
        self.dedent(indent)

    @staticmethod
    @contextmanager
    def create(path, **kwargs):
        '''Open (and later close) a CodeWriter that writes to a new file at the specified path.'''
        with open(path, 'w', **kwargs) as f:
            yield CodeWriter(f)


def as_rule_path(namespace, ypath):
    steps = ypath.split("/")
    selectors = []
    for s in steps:
        #if s.endswith("[1]"):
        #    print(s)
        if s == ".":
            pass
        elif s == "":
            if len(selectors) == 0 or selectors[-1] != "**":
                selectors.append("**")
        elif s == "$*":
            selectors.append("<>")
        elif namespace and namespace != "cobol":
            selectors.append("<%s:%s>" % (namespace, s))
        else:
            selectors.append("<%s>" % (s))
        
    return "/".join(selectors)

class Trigger:
    def __init__(self):
        pass

class Child(Trigger):
    def __init__(self, trigger):
        self.trigger = trigger

    def write(self, out):
        out.write('child(')
        self.trigger.write(out)
        out.write(')')

class ProgramText(Trigger):
    def __init__(self):
        pass

    def write(self, out):
        out.write('PROGRAM_TEXT')

class Start(Trigger):
    def __init__(self):
        pass

    def write(self, out):
        out.write('start()')

class IsLast(Trigger):
    def __init__(self, name = None, ns = "cobol"):
        self.name = name
        self.ns = ns

    def write(self, out):
        if self.name:
            out.write('isLast(Start.on("%s", "%s"))' % (self.ns, self.name))
        else:
            out.write('isLast(null)')

class IsLastChild(Trigger):
    def __init__(self, name = None, ns = "cobol"):
        self.name = name
        self.ns = ns

    def write(self, out):
        if self.name:
            out.write('isLastChild(Start.on("%s", "%s"))' % (self.ns, self.name))
        else:
            out.write('isLastChild(null)')

class Path(Trigger):
    def __init__(self, path):
        self.path = path

    def write(self, out):
        out.write('path("%s")' % self.path)

class Collect(Trigger):
    def __init__(self, trigger):
        self.trigger = trigger

    def write(self, out):
        out.write('collect(')
        self.trigger.write(out)
        out.write(')')

class First(Trigger):
    def __init__(self, trigger):
        self.trigger = trigger

    def write(self, out):
        out.write('first(')
        self.trigger.write(out)
        out.write(')')

class Or(Trigger):
    def __init__(self):
        self.triggers = []
    
    def add_all(self, triggers):
        self.triggers.extend(triggers)
    
    def write(self, out):
        if len(self.triggers) == 1:
            self.triggers[0].write(out)
        else:
            out.write('or(')
            self.triggers[0].write(out)
            out.write(", //\n")
            out.indent()
            for t in self.triggers[1:-1]:
                t.write(out)
                out.write(", //\n")
            self.triggers[-1].write(out)
            out.write(')')
            out.dedent()


class Effect:
    def __init__(self):
        pass

class TODO(Effect):
    def __init__(self, msg):
        self.msg = msg
        
    def write(self, out):
        out.write('TODO("%s")' % (self.msg.replace('"', '\\"')))

class Comment(Effect):
    def __init__(self, msg):
        self.msg = msg
        
    def write(self, out):
        out.write('// %s' % (self.msg))

class ApplyRule(Effect):
    def __init__(self, default):
        self.default = default
        
    def write(self, out):
        out.write('rules.applyRule(Start.on("cobol", "%s"))' % self.default)

class ApplyMatchingRule(Effect):
    def __init__(self, default):
        self.default = default
        
    def write(self, out):
        if self.default:
            # TODO Need a better way to do this.
            if self.default == "sql_other_stmt":
                out.write('rules.applyMatchingRule(Start.on("sql", "sql_other_stmt"))')
            else:
                out.write('rules.applyMatchingRule("%s")' % self.default)
        else:
            out.write('rules.applyMatchingRule()')

class CreateTuple(Effect):
    def __init__(self, type_name):
        self.type_name = type_name
        
    def write(self, out):
        out.write('createTuple("%s")' % (self.type_name))

class AtEnd(Effect):
    def __init__(self, effect):
        self.effect = effect
        
    def write(self, out):
        out.write('atEnd(')
        self.effect.write(out)
        out.write(')')

class AssignTo(Effect):
    def __init__(self, attr_name):
        self.attr_name = attr_name
        
    def write(self, out):
        out.write('assignTo("%s")' % (self.attr_name))

class All(Effect):
    def __init__(self):
        self.effects = []
    
    def add(self, effect):
        self.effects.append(effect)
    
    def write(self, out):
        if len(self.effects) == 1:
            self.effects[0].write(out)
        else:
            out.write('all(')
            self.effects[0].write(out)
            out.write(", //\n")
            out.indent()
            for effect in self.effects[1:-1]:
                effect.write(out)
                if isinstance(effect, Comment):
                    out.write("\n")
                else:
                    out.write(", //\n")
    
            self.effects[-1].write(out)
            out.write(')')
            out.dedent()

class NestedRecordStructure(Effect):
    def __init__(self, attribute_name, trigger):
        self.attribute_name = attribute_name
        self.trigger = trigger
    
    def write(self, out):
        out.write('nestedRecordStructure("')
        out.write(self.attribute_name)
        out.write('", ')
        self.trigger.write(out)
        out.write(', rules)')

class On(Effect):
    def __init__(self, trigger):
        self.trigger = trigger
        self.all = All()
        self.atEnd = None
    
    def add(self, effect):
        self.all.add(effect)
        
    def write(self, out):
        out.write('on(')
        self.trigger.write(out)
        out.indent()
        out.write(', //\n')
        self.all.write(out)
        if self.atEnd:
            out.write(', //\n')
            self.atEnd.write(out)
        out.write(')')
        out.dedent()

class SetAttribute(Effect):
    def __init__(self, name, trigger, default_ns="cobol", default=None):
        self.name = name
        self.trigger = trigger
        self.default_ns = default_ns
        self.default = default
    
    def write(self, out):
        out.write('setAttribute("')
        out.write(self.name)
        out.write('", ')
        self.trigger.write(out)
        out.write(', rules')
        if self.default:
            out.write(', Start.on("')
            out.write(self.default_ns)
            out.write('", "')
            out.write(self.default)
            out.write('")')
        out.write(')')

class SetAttributeAs(Effect):
    def __init__(self, name, trigger, rule_ns, rule):
        self.name = name
        self.trigger = trigger
        self.rule_ns = rule_ns
        self.rule = rule
    
    def write(self, out):
        out.write('setAttributeAs("')
        out.write(self.name)
        out.write('", ')
        self.trigger.write(out)
        out.write(', rules, Start.on("')
        out.write(self.rule_ns)
        out.write('", "')
        out.write(self.rule)
        out.write('"))')

class SetAttributeToProgramText(Effect):
    def __init__(self, name, trigger):
        self.name = name
        self.trigger = trigger
    
    def write(self, out):
        out.write('setAttributeToProgramText("')
        out.write(self.name)
        out.write('", ')
        self.trigger.write(out)
        out.write(')')

class Sub(Effect):
    def __init__(self):
        self.all = All()
    
    def add(self, effect):
        self.all.add(effect)
        
    def write(self, out):
        out.write('sub(')
        self.all.write(out)
        out.write(')')

class NumLines(Effect):
    def __init__(self):
        pass
    
    def write(self, out):
        out.write('NUMLINES')


p1 = re.compile(r"^\{(.*)\}$", re.IGNORECASE)
p_indexed_word = re.compile(r"^(\w+)(\[(\d+)\])?$", re.IGNORECASE)
p_last_word = re.compile(r"^(\w+)(\[last\(\)\])?$", re.IGNORECASE)
p_word_with_child = re.compile(r"^(\w+)(\[(\w+)\])?$", re.IGNORECASE)
namespaced = re.compile(r"^(\w+)::(.*$)", re.IGNORECASE)

def convert_from_ypath(path, ns="cobol"):
    if path == "$-1":
        return Child(IsLastChild())
    
    if path == "{(.//exit_node)[last()]}":
        return IsLast("exit_node", ns = "cflow")

    # TODO Get rid of this. Fix in spec ?
    if path == "$1": path = "{$1}"
    
    m = namespaced.match(path)
    if m:
        ns = m.group(1)
        path = m.group(2).strip()
    
    m = p1.match(path)
    if not m: return False
    
    ypath = m.group(1)
    choices = []
    for choice in ypath.split("|"):
        steps = []

        choice = choice.strip()
        if choice[0] == "/":
            if choice[1] != "/":
                # absolute path => first step is empty
                steps.append("")
            # remove leading slash in all cases
            choice = choice[1:]
        
        for step in choice.split("/"):
            index = None
            selector = None
            
            if step == ".":
                continue
            if step == "":
                selector = "**"
            elif step == "$1":
                selector = "<>"
                index = "1"
            elif step == "$*":
                selector = "<>"
            elif step == "..":
                selector = ".." # Beware, the semantics are subtly different for streams.
            
            if not selector:
                m = p_indexed_word.match(step)
                if m:
                    step = m.group(1)
                    index = m.group(3)
                    if ns == "cobol":
                        selector = "<%s>" % (step)
                    else:
                        selector = "<%s:%s>" % (ns, step)
            
            if not selector:
                m = p_last_word.match(step)
                if m:
                    step = m.group(1)
                    index = "-1"
                    if ns == "cobol":
                        selector = "<%s>" % (step)
                    else:
                        selector = "<%s:%s>" % (ns, step)
            
            if not selector:
                m = p_word_with_child.match(step)
                if m:
                    step = m.group(1)
                    index = "<%s>" % m.group(3)
                    if ns == "cobol":
                        selector = "<%s>" % (step)
                    else:
                        selector = "<%s:%s>" % (ns, step)
            
            if not selector: return False
            
            if index:
                steps.append("%s[%s]" % (selector, index))
            else:
                steps.append(selector)

        choices.append(Path("/".join(steps)))

    if len(choices) == 1:
        return choices[0]
    else:
        trigger = Or()
        trigger.add_all(choices)
        return trigger

def print_usage():
    print("Usage %s AST_FILE [OUTPUT]\n" % sys.argv[0])
    print("Generate a Java data model of the database scheme.")
    print("AST_FILE: The Python file containing the AST definition.")
    

def write_java_data_model(out, spec):
    '''Write the code for the Java data model to the given CodeWriter.'''
    module = __import__(spec)
    gen_name = '/'.join(__file__.split(os.path.sep)[-2:])
    ast_types = wordnum_sorted(ast_types_from_module(module))

    out.write(cobol_populator_preamble % gen_name)
    out.indent(levels=2)

    write_rules(out, ast_types)

    out.dedent(levels=2)
    out.write(cobol_populator_close)


def write_rules(out, ast_types):
    for ast_type in ast_types:
        write_rule(out, ast_type)

p_default = re.compile(r"^(.*)\s+default\s+(\w+)$", re.IGNORECASE)
p_as = re.compile(r"^(.*)\s+as\s+(\w+)$", re.IGNORECASE)


def write_rule(out, ast_type):
    if not isinstance(ast_type, NodeType): return
    if not ast_type.is_case_type(): return

    type_name = ast_type.name
    node_name = ast_type.node_name
    
    # We do these manually in the streamed version.
    if node_name == "copybook" or node_name == "compilationGroup": return
    
    node_path = node_name
    if node_path is None: return
    else: node_path = node_path.strip()

    namespace = ast_type.node_ns
    if node_path.startswith("<SQL> "):
        namespace = "sql"
        node_path = node_path[6:].strip()
    
    rule_selector = as_rule_path(namespace, node_path)

    effects = All()
    effects.add(CreateTuple(type_name))

    attribute_names = ast_type.all_attribute_names
    for attribute_name in attribute_names:
        attr = ast_type.resolve_attribute(attribute_name)
        parser_paths = attr.parser_paths
        attr_ns = namespace
        
        nested_record_structure = False
        if attr.artificial or parser_paths is None:
            continue

        elif len(parser_paths) == 2 and parser_paths[1] == "[NESTED_RECORD_STRUCTURE]":
            nested_record_structure = True
            
        elif len(parser_paths) == 2 and parser_paths[0] == "using <SQL>":
            attr_ns = "sql"
            parser_paths = [parser_paths[1].strip()]
            
        elif len(parser_paths) > 1:
            effects.add(TODO('%s.%s <- %s' % (type_name, attribute_name, parser_paths)))
            continue
            
        primitive = isinstance(attr.ast_type, PrimitiveType)
        primitive_list = isinstance(attr.ast_type, ListType) and isinstance(attr.ast_type.item_type, PrimitiveType)

        expr = parser_paths[0]
        
        if nested_record_structure:
            expr = expr[len("map "):]
        
        # TODO Clean this override up when the old mapping has been replaced.
        if expr == "{statement|nestedStatements|compilerStatement}":
            expr = "{statement|nestedStatements/statement|compilerStatement} default other_stmt"
        
        path = expr
        
        default_rule = None
        m = p_default.match(path)
        if m:
            path = m.group(1)
            default_rule = m.group(2)
            
        as_rule = None
        m = p_as.match(path)
        if m:
            path = m.group(1)
            as_rule = m.group(2)
        
        trigger = convert_from_ypath(path, ns=attr_ns)

        if trigger:
            if nested_record_structure:
                effects.add(Comment('%s.%s <- nested record structure %s' % (type_name, attribute_name, expr)))
                effects.add(NestedRecordStructure(attribute_name, trigger))
                continue
            if primitive and (as_rule or default_rule):
                effects.add(TODO('PRIMITIVE %s.%s <- %s' % (type_name, attribute_name, expr)))
                continue
            elif primitive_list and (as_rule or default_rule):
                effects.add(TODO('PRIMITIVE LIST %s.%s <- %s' % (type_name, attribute_name, expr)))
                continue

            effects.add(Comment('%s.%s <- %s' % (type_name, attribute_name, expr)))
            
            if primitive or primitive_list:
                effects.add(SetAttributeToProgramText(attribute_name, trigger))
            elif as_rule:
                if as_rule.startswith("sql_"):
                    effects.add(SetAttributeAs(attribute_name, trigger, "sql", as_rule))
                else:
                    effects.add(SetAttributeAs(attribute_name, trigger, "cobol", as_rule))
            else:
                if default_rule and default_rule.startswith("sql_"):
                    effects.add(SetAttribute(attribute_name, trigger, "sql", default_rule))
                else:
                    effects.add(SetAttribute(attribute_name, trigger, "cobol", default_rule))
            
        elif path == "$.":
            if primitive and (as_rule or default_rule):
                effects.add(TODO('PRIMITIVE %s.%s <- %s' % (type_name, attribute_name, expr)))
                continue
            elif primitive_list and (as_rule or default_rule):
                effects.add(TODO('PRIMITIVE LIST %s.%s <- %s' % (type_name, attribute_name, expr)))
                continue
            
            effects.add(Comment('%s.%s <- %s' % (type_name, attribute_name, expr)))
            sub = Sub()

            if primitive or primitive_list:
                sub.add(Collect(ProgramText()))
                sub.add(AtEnd(AssignTo(attribute_name)))
            elif as_rule:
                sub.add(ApplyRule(as_rule))
                sub.add(AtEnd(AssignTo(attribute_name)))
            else:
                sub.add(ApplyMatchingRule(default_rule))
                sub.add(AtEnd(AssignTo(attribute_name)))

            effects.add(sub)
            
        else:
            effects.add(TODO('%s.%s <- %s' % (type_name, attribute_name, expr)))
            continue
    
    for also in ast_type.all_additional_traps:
        if also == "NUMLINES":
            effects.add(Comment('[NUMLINES]'))
            effects.add(NumLines())
        elif also == "HALSTEAD":
            effects.add(Comment('[HALSTEAD]'))
        else:
            effects.add(TODO('ALSO %s : %s' % (type_name, also)))

    out.write("// %s ...\n" % (node_name))
    out.write('rules.define("%s", //\n' % (rule_selector))
    out.indent()

    effects.write(out)

    out.write(' //\n')
    out.dedent()
    out.write(');\n\n')





cobol_populator_preamble = """package com.semmle.cobol.population;

import com.semmle.cobol.generator.rules.RuleSet;

import static com.semmle.cobol.generator.effects.Effects.NUMLINES;
import static com.semmle.cobol.generator.effects.Effects.all;
import static com.semmle.cobol.generator.effects.Effects.assignTo;
import static com.semmle.cobol.generator.effects.Effects.atEnd;
import static com.semmle.cobol.generator.effects.Effects.collect;
import static com.semmle.cobol.generator.effects.Effects.createTuple;
import static com.semmle.cobol.generator.effects.Effects.nestedRecordStructure;
import static com.semmle.cobol.generator.effects.Effects.on;
import static com.semmle.cobol.generator.effects.Effects.print;
import static com.semmle.cobol.generator.effects.Effects.sub;
import static com.semmle.cobol.generator.effects.Effects.setAttribute;
import static com.semmle.cobol.generator.effects.Effects.setAttributeAs;
import static com.semmle.cobol.generator.effects.Effects.setAttributeToProgramText;

import static com.semmle.cobol.generator.triggers.Triggers.child;
import static com.semmle.cobol.generator.triggers.Triggers.end;
import static com.semmle.cobol.generator.triggers.Triggers.first;
import static com.semmle.cobol.generator.triggers.Triggers.isLast;
import static com.semmle.cobol.generator.triggers.Triggers.isLastChild;
import static com.semmle.cobol.generator.triggers.Triggers.PROGRAM_TEXT;
import static com.semmle.cobol.generator.triggers.Triggers.or;
import static com.semmle.cobol.generator.triggers.Triggers.path;
import static com.semmle.cobol.generator.triggers.Triggers.start;

import koopa.core.data.markers.Start;

/**
 * This class is auto-generated by '%s', from the same data
 * definition as the Cobol database schema and QL wrapper classes.
 */
public class CobolRulesFromSpec {
    public static void initialize(RuleSet rules) {
"""

cobol_populator_close="""    }
}
"""


def main(args):
    '''Write the code for the abstract extractor visitor.'''
    if len(args) == 3:
        with CodeWriter.create(args[2], encoding='utf-8', newline='') as out:
            write_java_data_model(out, args[1])
    elif len(args) == 2:
        out = CodeWriter(sys.stdout)
        write_java_data_model(out, args[1])
    else:
        print_usage()
        sys.exit(-1)

if __name__ == '__main__':
    main(sys.argv)
