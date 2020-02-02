#!/usr/bin/env python3

import sys
import re
import os
import subprocess
import argparse
import gzip

nodes = []
named_nodes = {}

class Node(object):
    def __init__(self, id=None, category=None, label=None):
        self.id = id
        self.category = category
        self.label = label

        self._index = len(nodes)
        nodes.append(self)

        if id:
            named_nodes[id] = self

edges = []

class Edge(object):
    def __init__(self, source, target, label=None):
        self.source = source
        self.target = target
        self.label = label

        self._index = len(edges)
        edges.append(self)


def parse_args(args):
    arg_parser = argparse.ArgumentParser(description='Visualize trap file')
    arg_parser.add_argument('path', help='Path to QL files.')
    return arg_parser.parse_args(args)


RELATION = re.compile('class (\w+)(?: extends (.*) \{)?')

def get_node_for_class(name):
    if name in named_nodes:
        return named_nodes[name]
    else:
        return Node(id=name, label=name)

def process(qll_file):
    with open(qll_file) as input:
        for line in input:
            line = line.rstrip()
            
            match = RELATION.search(line)
            if match:
                class_name = match.group(1)
                class_node = get_node_for_class(class_name)

                super_names = match.group(2)
                if super_names:
                    super_names = super_names.split(', ')
                    for sn in super_names:
                        super_node = get_node_for_class(sn)
                        Edge(class_node, super_node)
                        # print("%s <: %s" % (class_name, sn))
                #else:
                #    print("%s" % class_name)

    print("digraph {")
    
    for e in edges:
        print('  N%i -> N%i;' % (e.source._index, e.target._index))
    
    for n in nodes:
        print('  N%i [label="%s"; name="%s"];' % (n._index, n.label.replace('"','\\"'), n.id))
    
    print("}")
    

def main(args):
    opts = parse_args(args[1:])

    for dir, subdirs, files in os.walk(opts.path):
        for f in files:
            if f.endswith(".qll"):
                process("%s" % os.path.join(dir, f))

if __name__ == '__main__':
    main(sys.argv)
