'''Tool for generating the Java data model.'''

import io, os, re, sys

from contextlib import contextmanager
from operator import attrgetter

from AST import AstType, PrimitiveType, ClassType, UnionType, ListType
from gentools import ast_types_from_module, wordnum_sorted, get_disjunction_names, get_main_relation, get_primitive_attribute_relation, get_primitive_list_item_relation

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
        self.indent(indent)
        if len(code) == 0:
            return
        # Split into lines, preserving newline characters
        lines = [ l + '\n' for l in code.split('\n') ]
        if not code[-1].endswith('\n'):
            lines[-1] = lines[-1][:-1] # Last line is not terminated, so remove appended newline
        else:
            lines = lines[:-1] # Remove empty string appended to indicate final newline
        for line in lines:
            if self.indent_next:
                if line != '\n':
                    self.out.write(('%%%ds' % (self.indent_level * self.indent_size)) % '')
                self.indent_next = False
            self.out.write(line)
            self.indent_next = len(line) > 0 and line[-1] == '\n'
        self.dedent(indent)

    @staticmethod
    @contextmanager
    def create(path, **kwargs):
        '''Open (and later close) a CodeWriter that writes to a new file at the specified path.'''
        with open(path, 'w', **kwargs) as f:
            yield CodeWriter(f)

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
    
    write_types(out, ast_types)
    write_tables(out, ast_types)
        
    out.dedent(levels=2)
    out.write(cobol_populator_close)

def write_types(out, ast_types):
    out.write('// Original types ...\n\n')
    for ast_type in ast_types:
        write_type(out, ast_type)

def write_type(out, ast_type):
    type_name = ast_type.name
    
    if isinstance(ast_type, PrimitiveType):
        name = type_name
        type_name = type_name + '_primitive'
        db_type = prim_map[ast_type._key].java_db_type
        ql_type = prim_map[ast_type._key].java_ql_type
        out.write('PrimitiveType %s = new PrimitiveType("%s", %s, %s);\n'
            % (type_name, name, db_type, ql_type))

    elif isinstance(ast_type, UnionType): 
        out.write('UnionType %s = new UnionType("%s"' % (type_name, type_name))
        for member in wordnum_sorted(ast_type.get_members()):
            out.write(', "%s"' % member.name)
        out.write(");\n")
    
    elif isinstance(ast_type, ListType):
        item_name = ast_type.item_type.name
        out.write('ListType %s = new ListType("%s", "%s");\n' % (type_name, type_name, item_name))

    elif isinstance(ast_type, ClassType): 
        if ast_type.is_base_case_type():
            out.write('BaseCaseType %s = new BaseCaseType("%s");\n' % (type_name, type_name))

        else:
            base_name = ast_type.case_base_type.name
            index = ast_type.case_index
            out.write('CaseType %s = new CaseType("%s", "%s", %i);\n' % (type_name, type_name, base_name, index))
        
    else:
        raise ValueError("Don't know how to generate code for a %s." % type_name)

    out.write('typeSystem.addType(%s);\n' % type_name)

    for attr in ast_type.attributes:
        attr_name = attr.name
        attr_type = attr.ast_type
        attr_type_name = attr_type.name
        
        index = None
        if attr_type.is_union():
            if any(t.requires_index() for t in attr_type.get_all_members()):
                index = attr.index
        elif attr_type.requires_index():
            index = attr.index

        if attr.parent_type.is_union():
            # Attributes for union types are abstract.
            pass
        elif attr.dbscheme_ignore:
            pass
        elif index is None:
            out.write('%s.addAttribute("%s", "%s");\n' % (type_name, attr_name, attr_type_name))
        else:
            out.write('%s.addAttribute("%s", "%s", %i);\n' % (type_name, attr_name, attr_type_name, index))
        
    out.write('\n')

def write_tables(out, ast_types):
    out.write('// Relations ...\n\n')
    disjunction_names = get_disjunction_names(ast_types)
    
    for ast_type in ast_types:
        if isinstance(ast_type, UnionType):
            continue

        relation = get_main_relation(ast_type, disjunction_names)
        if relation:
            out.write('%s.setRelationName("%s");\n' % (ast_type.name, relation.name))
            write_relation(out, relation)
            out.write('\n')

        for attr in ast_type.attributes:
            if attr.dbscheme_ignore:
                continue
            attr_relation = get_primitive_attribute_relation(attr)
            if attr_relation:
                out.write('Partition %s_partition = new Partition("%s", %s.getAttribute("%s").getTypeName());\n' % (attr_relation.name, attr_relation.name, ast_type.name, attr.name))
                out.write('%s_partition.setParentColumn("%s");\n' % (attr_relation.name, 'id'))
                out.write('%s_partition.setValueColumn("%s");\n' % (attr_relation.name, attr.name))
                out.write('%s_partition.setRelationName("%s");\n' % (attr_relation.name, attr_relation.name))
                out.write('%s.getAttribute("%s").setTypeName("%s");\n' % (ast_type.name, attr.name, attr_relation.name))
                out.write('typeSystem.addType(%s_partition);\n' % attr_relation.name)
                out.write('\n')
                
                write_relation(out, attr_relation)
                out.write('\n')

        if ast_type.is_list() and ast_type.item_type.is_primitive():
            item_relation = get_primitive_list_item_relation(ast_type)
            if item_relation:
                out.write('Partition %s_partition = new Partition("%s", %s.getItemTypeName());\n' % (item_relation.name, item_relation.name, ast_type.name))
                out.write('%s_partition.setParentColumn("%s");\n' % (item_relation.name, 'parent'))
                out.write('%s_partition.setValueColumn("%s");\n' % (item_relation.name, 'item'))
                out.write('%s_partition.setRelationName("%s");\n' % (item_relation.name, item_relation.name))
                out.write('%s.setItemTypeName("%s");\n' % (ast_type.name, item_relation.name))
                out.write('typeSystem.addType(%s_partition);\n' % item_relation.name)
                out.write('\n')

                write_relation(out, item_relation)
                out.write('\n')
        

def write_relation(out, relation):
    rel_name = relation.name
    out.write('Relation %s_relation = new Relation("%s");\n' % (rel_name, rel_name))
    out.write('dbScheme.addRelation(%s_relation);\n' % rel_name)
    
    for column in relation.columns:
        db_type = prim_map[column.db_type].java_db_type
        out.write('%s_relation.addColumn("%s", %s);\n' % (rel_name, column.column_name, db_type))
    


cobol_populator_preamble = """package com.semmle.cobol.population;

import com.semmle.cobol.generator.tables.DatabaseScheme;
import com.semmle.cobol.generator.tables.Relation;
import com.semmle.cobol.generator.types.DBType;
import com.semmle.cobol.generator.types.CaseType;
import com.semmle.cobol.generator.types.ListType;
import com.semmle.cobol.generator.types.Partition;
import com.semmle.cobol.generator.types.PrimitiveType;
import com.semmle.cobol.generator.types.QLType;
import com.semmle.cobol.generator.types.BaseCaseType;
import com.semmle.cobol.generator.types.TypeSystem;
import com.semmle.cobol.generator.types.UnionType;

/**
 * This class is auto-generated by '%s', from the same data
 * definition as the Cobol database schema and QL wrapper classes.
 *
 * The single method is used to populate the extractor's internal
 * representation of the database schema ({@link DatabaseScheme}) and the
 * higher-level data model using 'types' with 'attributes' ({@link TypeSystem}).
 */
public class CobolPopulator {
    public static void populate(DatabaseScheme dbScheme, TypeSystem typeSystem) {
"""

cobol_populator_close="""    }
}
"""

class PrimMap():
    def __init__(self, java_db_type, java_ql_type):
        self.java_db_type = java_db_type
        self.java_ql_type = java_ql_type

prim_map = {
    "varchar(1)": PrimMap("DBType.VARCHAR", "QLType.STRING"),
    "unique int": PrimMap("DBType.INT", "QLType.INT"),
    "int": PrimMap("DBType.INT", "QLType.INT"),
    "float": PrimMap("DBType.FLOAT", "QLType.FLOAT"),
    "boolean": PrimMap("DBType.BOOLEAN", "QLType.BOOLEAN"),
    "date": PrimMap("DBType.DATE", "QLType.DATE")
}

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
