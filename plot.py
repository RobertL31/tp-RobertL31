# -*- coding: utf-8 -*-
"""
Created on Tue May 11 18:03:26 2021

@author: loica
"""

import matplotlib.pyplot as plt

file = open("score.txt", "r")
lines = file.readlines()

x = []

for line in lines:
    x.append(int(line))
    
    
plt.plot(x, color = "red", marker='.', linestyle = "dotted", markersize=5, markerfacecolor='blue')
plt.title("Jobshop optimization")
plt.xlabel("iteration number")
plt.ylabel("makespan")
plt.ylim(bottom = 0)
plt.savefig("plot.pdf")
plt.show()