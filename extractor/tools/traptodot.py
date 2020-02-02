#!/usr/bin/env python3

import sys
import re
import os
import subprocess
import argparse
import gzip

ID = re.compile('^#\d+$')
ID_DECLARATION = re.compile('^(#\d+)=(.*)$')
RELATION = re.compile('^(\w+)\(((?:[^,]+)(?:,(?:[^,]+))*)\)$')

nodes = []
named_nodes = {}

class Node(object):
    def __init__(self, line_number, id=None, category=None, label=None):
        self.line_number = line_number
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
    arg_parser.add_argument('trapfile', help='Path to trap file to visualize.')
    return arg_parser.parse_args(args)

def convert(trapfile):
    line_number = 0
    for line in trapfile:
        line_number += 1
        line = line.rstrip()

        match = ID_DECLARATION.match(line)
        if match:
            id = match.group(1)
            label = match.group(2)
            node = Node(line_number, id=id, category="#", label=id)
            continue

        match = RELATION.match(line)
        if match:
            relation = match.group(1)
            values = match.group(2).split(',')

            node = Node(line_number, id=relation, category=relation, label="%s(...)" % (relation))
            labels = []
            for v in values:
                if ID.match(v):
                    index = len(labels)
                    target = named_nodes[v]
                    edge = Edge(node, target, label="%i"% index)
                    labels.append('$%i' % (index))
                else:
                    labels.append(v)
                
            node.label = "%s(%s)" % (relation, ','.join(labels))
            continue

        raise ValueError('Unrecognized line %i: %s' % (line_number, line))

    print("digraph {")
    
    for e in edges:
        print('  N%i -> N%i [label="%s"];' % (e.source._index, e.target._index, e.label.replace('"','\\"')))
    
    for n in nodes:
        print('  N%i [label="%s"; name="%s"; category="%s"];' % (n._index, n.label.replace('"','\\"'), n.id, n.category))
    
    print("}")

    #for n in nodes:
    #    print("%i - %s" % (n.line_number, n.label))
    #print("Number of nodes: %i" % len(nodes))

    #for e in edges:
    #    print("%s" % (e.label))
    #print("Number of edges: %i" % len(edges))


def main(args):
    opts = parse_args(args[1:])
    
    if (opts.trapfile.endswith(".gz")):
        with gzip.open(opts.trapfile, 'rt') as trapfile:
                convert(trapfile)
    else:
        with open(opts.trapfile) as trapfile:
            convert(trapfile)

if __name__ == '__main__':
    main(sys.argv)
