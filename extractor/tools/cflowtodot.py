#!/usr/bin/python

import sys
import re
import os
import subprocess
import argparse

node_string =  r'([^|]|(\|\|))+' # Matches QL `toString()` identifiers
one_node = r'\|\s*([^:]+:(\d+):\d+:\d+:\d+)\s*\|\s*(' + node_string + ')\s*'
regex = r'^' + one_node + one_node + '\|$'


def parse_args(args):
    arg_parser = argparse.ArgumentParser(description='Visualize control flow graph')
    arg_parser.add_argument('-p', default='Successors.expected', help='Old/previous control flow (default: "%(default)s")')
    arg_parser.add_argument('-n', default='Successors.actual', help='New/current control flow (default: "%(default)s")')
    arg_parser.add_argument('-o', default='Successors.gv', help='Output file (default: "%(default)s")')
    return arg_parser.parse_args(args)

def run_successors_query():
    if os.path.exists("Successors.ql"):
        sys.stderr.write("Successors.ql already exists but no output file found or specified")
        sys.exit(1)
    queryfile = open("Successors.ql", "w")
    queryfile.write("import cobol\n")
    queryfile.write("from CFlowNode n\n")
    queryfile.write("select n, n.getASuccessor()\n")
    queryfile.close()
    subprocess.call("odasa qltest Successors.ql", stdout=sys.stderr, shell=True)
    os.remove("Successors.ql")

def parse_query_output(fn):
    nodes = {}
    edges = {}
    with open(fn, 'r') as myfile:
        for line in myfile.xreadlines():
            m = re.match(regex, line)
            if m:
                n1_id = m.group(3) + ':' + m.group(1).strip()
                n1_name = m.group(3).strip() + " (line " + m.group(2) + ")"
                n2_id = m.group(8) + ':' + m.group(6).strip()
                n2_name = m.group(8).strip() + " (line " + m.group(7) + ")"

                for n_id, name in [(n1_id, n1_name),(n2_id, n2_name)]:
                    if n_id in nodes and nodes[n_id] != name:
                        sys.stderr.write("Name clash for id '%s' ('%s' and '%s')" % (n_id, nodes[n_id], name))
                        sys.exit(1)
                nodes[n1_id] = n1_name
                nodes[n2_id] = n2_name
                if n1_id not in edges:
                    edges[n1_id] = []
                edges[n1_id].append(n2_id)
    return nodes, edges

opts = parse_args(sys.argv[1:])
opts.compare = os.path.exists(opts.p) and os.path.exists(opts.n)
if not os.path.exists(opts.p) and not os.path.exists(opts.n):
    run_successors_query()
elif not opts.compare and os.path.exists(opts.p):
    # Just use the 'previous' file
    opts.n = opts.p


with open(opts.o, 'w') as gv:
    gv.write("digraph G {\n")
    new_nodes, new_edges = parse_query_output(opts.n)
    if opts.compare:
        old_nodes, old_edges = parse_query_output(opts.p)

        all_nodes = set.union(set(new_nodes.keys()), set(old_nodes.keys()))
        for n in all_nodes:
            if n not in new_nodes:
                label = old_nodes[n]
                color = 'red'
            elif n not in old_nodes:
                label = new_nodes[n]
                color = 'green'
            else:
                label = old_nodes[n]
                color = 'black'
            gv.write('"%s" [label="%s" color="%s"];\n' % (n, label, color))

        # Make edge relations have the same domain
        for n in all_nodes:
            for edges in [old_edges, new_edges]:
                if n not in edges:
                    edges[n] = []
                
        for n1 in set.union(set(new_edges.keys()), set(old_edges.keys())):
            for n2 in set.union(set(new_edges[n1]), set(old_edges[n1])):
                if n2 not in new_edges[n1]:
                    color = 'red'
                elif n2 not in old_edges[n1]:
                    color = 'green'
                else:
                    color = 'black'
                gv.write('"%s" -> "%s" [color="%s"];\n' % (n1, n2, color))
    else:
        for n in new_nodes:
            gv.write('"%s" [label="%s"];\n' % (n, new_nodes[n]))
        for n1 in new_edges:
            for n2 in new_edges[n1]:
                gv.write('"%s" -> "%s";\n' % (n1, n2))

    gv.write("}\n")
