__author__ = 'zhuozhongliu'
import numpy as np
import matplotlib.pyplot as plt



def plotHeatmap(title, xlabel, ylabel, data):

    plt.figure(figsize=(10, 6))
    plt.title(title)
    plt.xlabel(xlabel)
    plt.ylabel(ylabel)
    v1 = abs(data).max()
    v2 = abs(data).min()
    c = plt.pcolor(data,  linewidths=5, cmap='Greys', vmin=v2, vmax=v1)

    plt.yticks(np.arange(0.5, data.shape[1]), range(0, data.shape[1]))
    plt.xticks(np.arange(0.5, data.shape[1]), range(0, data.shape[1]))
    plt.xlim((0, data.shape[1]))
    plt.ylim((0, data.shape[1]))
    #plt.xlim((0, AUC.shape[1]))
    show_values(c)

    plt.colorbar(c)
    plt.show()

def show_values(pc, fmt="%d", **kw):
    from itertools import izip
    pc.update_scalarmappable()
    ax = pc.get_axes()
    for p, color, value in izip(pc.get_paths(), pc.get_facecolors(), pc.get_array()):
        x, y = p.vertices[:-2, :].mean(0)
        if np.all(color[:3] > 0.5):
            color = (0.0, 0.0, 0.0)
        else:
            color = (1.0, 1.0, 1.0)
        if value==0:
            continue
        else:
            if x==y:
                ax.text(x, y, fmt % value, ha="center", va="center", color=color, **kw)
            if x!= y:
                ax.text(x, y, fmt % value, ha="center", va="center", color=(0.0, 0.0, 0), **kw)

#plotHeatmap("ROC's AUC", "x", "y",np.random.rand(8,12))