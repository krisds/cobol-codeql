#!/usr/bin/env python3

'''Tool for generating a CSV from Timing logs.'''

import io, os, re, sys

MARKER = "(x) "

START_OF = re.compile(r"^start of (.*) at \d+$", re.IGNORECASE)
END_OF = re.compile(r"^end of (.*) at \d+$", re.IGNORECASE)
TIME = re.compile(r"^(.*) : (\d+)ms$", re.IGNORECASE)

def convert_timing_info(input, output):
    depth = 0
    processes = set()
    all_timings = []
    current_timings = None
    
    line = input.readline()
    while line:
        if line.startswith(MARKER):
            line = line[len(MARKER):]
            
            m = START_OF.match(line)
            if m:
                if depth == 0:
                    current_timings = {}
                    all_timings.append(current_timings)
                    
                    source = m.group(1)
                    current_timings['source'] = source

                elif depth == 1:
                    process = m.group(1)
                    processes.add(process)
                depth += 1
            
            m = END_OF.match(line)
            if m:
                process = m.group(1)
                depth -= 1
            
            m = TIME.match(line)
            if m:
                process = m.group(1)
                ms = m.group(2)
                if depth == 0:
                    current_timings['all'] = ms
                elif depth == 1:
                    current_timings[process] = ms
        
        line = input.readline()
    
    names = sorted(processes)
    output.write("source,all,%s\n" % (",".join(names)))
    for t in all_timings:
        output.write("%s,%s,%s\n" % (t['source'], t['all'], ",".join([t[n] if n in t else '' for n in names])))


def main(args):
    '''Write the code for the abstract extractor visitor.'''
    convert_timing_info(sys.stdin, sys.stdout)

if __name__ == '__main__':
    main(sys.argv)
