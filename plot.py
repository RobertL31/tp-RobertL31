# -*- coding: utf-8 -*-
"""
Created on Tue May 11 18:03:26 2021

@author: loica
"""

import matplotlib.pyplot as plt
import sys


if __name__ == "__main__":
    if len(sys.argv) != 4:
        print("error, to few arguments given to " + sys.argv[0])
        exit(1)
    
    target_directory = sys.argv[1]
    instance_name = sys.argv[2]


    file = open("score.txt", "r")
    lines = file.readlines()

    final  = []
    x = []
    solverNames = []

    for line in lines:
        if line.isnumeric():
            x.append(int(line))
        else:
            final.append(x)
            solverNames.append(line)

    colors = ["red", "blue"]
            
        
    plt.plot(final, marker='.', linestyle = "dotted", markersize=1, markerfacecolor=colors, label=solverNames)
    plt.title("Jobshop optimization")
    plt.xlabel("iteration number")
    plt.ylabel("makespan")
    plt.ylim(bottom = 0)
    path = target_directory + "/" + instance_name + ".pdf"
    plt.savefig(path)