#this file is used for stats the result for WF gene test

import sys
import os
import re
import string

#import heatmapdraw as heat
#import numpy as np
#from sklearn.metrics import *
#import numpy as np
#import matplotlib.pyplot as plt
#from sklearn import metrics
#from sklearn.metrics import classification_report
resultPath =sys.argv[1]
test_persite=1
correct_count =0.0
totalqueries_count =0.0
pattern = re.compile(r'Domain annotation for each model \(and alignments\):\n>> (\d+)')
pattern2 = re.compile('(\\[No hits detected that satisfy reporting thresholds\\])')
confusematrix=None

y_true=[]
y_predict=[]

classIDmap=dict()

TP=0.0
FN=0.0
miss=0.0
def readcurrentFile(filename,path):
    global correct_count
    global totalqueries_count
    global confusematrix
    global classIDmap
    global TP, FN,miss
    file_object = open(path)
    all_the_text = file_object.read( )
    correctAnswer = filename.split("_")[1]
    correctAnswer =string.atoi(correctAnswer)

    for i in range(test_persite):
        y_true.append(correctAnswer)
    #print(all_the_text)
    match = pattern.findall(all_the_text)  # match is the test answer
    match2 = pattern2.findall(all_the_text)
    if match2:
        print("---->" +filename)

    if len(match)==0:
        miss=miss+len(match2)
        totalqueries_count = totalqueries_count + len(match2)
    else:
        miss = miss + len(match2)
        totalqueries_count = totalqueries_count + len(match)
        totalqueries_count = totalqueries_count + len(match2)
    #print(match)
    Currentinstance_count=0
    for mytestAnswer in match:
        test = string.atoi(mytestAnswer)
        y_predict.append(test)
        if test==correctAnswer:
            correct_count=correct_count+1
            TP=TP+1
            confusematrix[classIDmap[correctAnswer]][classIDmap[test]]=confusematrix[classIDmap[correctAnswer]][classIDmap[test]] + 1
        else:
            FN=FN+1
            confusematrix[classIDmap[correctAnswer]][classIDmap[test]] = confusematrix[classIDmap[correctAnswer]][classIDmap[test]] + 1
            print filename+" "+ str(Currentinstance_count)
        Currentinstance_count=Currentinstance_count+1


    file_object.close()


print("reading " +resultPath)


list = sorted(os.listdir(resultPath),key=lambda x: int(x.split("_")[1]))
classid=0
for filename in list:
    thisid =int(filename.split("_")[1])
    if classIDmap.has_key(thisid):
        continue
    else:
        classIDmap[thisid]=classid
        classid=classid+1


print(len(list))

confusematrix=[[0 for col in range(len(list))] for row in range(len(list))]

class_names=[i for i in range(len(list))]
for line in list:
    filepath =os.path.join(resultPath,line)
    if os.path.isdir(filepath):
        continue
    else:
        readcurrentFile(line,filepath)

print ("%d/%d"%(correct_count,totalqueries_count))
print("ACC:")
print(correct_count/totalqueries_count)

# Sensitivity, hit rate, recall, or true positive rate
#TPR = TP / (TP + FN)
# Specificity or true negative rate
#TNR = TN / (TN + FP)

# Fall out or false positive rate
#FPR = FP / (FP + TN)
# False negative rate
#FNR = FN / (TP + FN)


# Overall accuracy
#ACC = (TP + TN) / (TP + FP + FN + TN)
print("TP: "+ str(TP) +"\t"+"FN: "+ str(FN)+"\t"+"Miss: "+ str(miss)+"\t")
#print("precision: " + str(metrics.precision_score(np.array(y_true), np.array(y_predict))))
#print("Recall: " + str(metrics.recall_score(np.array(y_true), np.array(y_predict))))
#print("f1_score: " + str(metrics.f1_score(np.array(y_true), np.array(y_predict))))

#heat.plotHeatmap("", "Site class ID", "Predicted class ID", np.asarray(confusematrix).transpose())
#print(classification_report(np.array(y_predict),np.array(y_true)))



# plt.figure()
# cm= np.array(confusematrix)
# plt.plot_confusion_matrix(cm, classes=class_names,title='Confusion matrix, without normalization')
# plt.show()
# for line in confusematrix:
#     print(str(line)+";")
