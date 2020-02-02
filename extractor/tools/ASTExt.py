'''Extension to the AST module to provide some things required by the Cobol generator scripts.'''
from AST import ClassType

class NodeType(ClassType):
    ql_child = True

    def __init__(self, name, base_type=None, node_name=None, node_ns="cobol"):
        '''Similar to ClassType.__init__, but with an optional node_name
           keyword. Reusing parser_name was not an option, as it is given a
           default value. We, however, want to know whether that value was
           explicitly set or not.'''
        self.node_name = node_name
        self.node_ns = node_ns
        self._also_trap = []
        ClassType.__init__(self, name, base_type=base_type)
        
    def also_trap(self, expr):
        '''Similar to what we store in parser_paths on attributes, except
           that this applies to the type.'''
        self._also_trap.append(expr)

    @property
    def all_additional_traps(self):
        '''Returns a list of all additional trap expressions, including those
           defined on base types.'''
        additional = []
        additional.extend(self._also_trap)
        if isinstance(self.case_base_type, NodeType):
            additional.extend(self.case_base_type.all_additional_traps)
        return additional
    
    @property
    def all_attribute_names(self):
        '''Returns a list of all attribute names, including those defined on
           base types. The order of definition is kept, with those defined by
           the base type coming first.'''
        names = []

        if self.case_base_type:
            names.extend(self.case_base_type.all_attribute_names)

        for attr in self._attributes:
            if not attr.name in names:
                names.append(attr.name)
        
        return names

    def resolve_attribute(self, name):
        '''Look up an attribute for the given name. If not found, look it up
           in the base type instead.'''
        for f in self._attributes:
            if f.name == name:
                return f
                
        if self.case_base_type:
            return self.case_base_type.resolve_attribute(name)
        else:
            raise ValueError(name)

