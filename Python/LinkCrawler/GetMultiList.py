__author__ = 'zhuozhongliu'

from Webcrawler import getAllExternalLinks
import  os
from urlparse import urlparse
import tldextract
outdir="WebsiteAdded"
filename="sitemaps.txt"
MainSiteFile=open("AllnewTrain.txt")
sitesperinstance_in=10
sitesperinstance_out=5

# for line in MainSiteFile.readlines():
#     tmpline =line[7:-1]
#     if not os.path.exists(outdir+"/"+tmpline):
#         os.makedirs(outdir+"/"+tmpline)

def getinternalLinks(alllinks,hostname):
    uniqueSubDomainset= set()
    uniqueExternalLinks=set()
    mydomain=tldextract.extract(hostname)[1]
    mysubdomain = tldextract.extract(hostname)[0]
    for line in alllinks:
        if line:
            domain_parts = tldextract.extract(line)
            if domain_parts[1] == mydomain:      # baidu
                o = urlparse(line)
                uniqueSubDomainset.add(o.geturl())
            else:
                o = urlparse(line)
                uniqueExternalLinks.add(o.geturl())

    return uniqueSubDomainset,uniqueExternalLinks


current =0
for line in MainSiteFile.readlines():
    current=current+1
    print(str(current) + " "+ line)
    outdirpath = outdir + "/" + line[7:-1] + "/"
    outfilepath = outdir+"/"+line[7:-1]+"/"+filename
    alllinks = getAllExternalLinks(line)
    filteredInLinks, filteredOutLinks=getinternalLinks(alllinks,line)
    if not os.path.exists(outdirpath):
        os.makedirs(outdirpath)
    outfileWriter=open(outfilepath,"a")
    if len(alllinks)==0:
        print("------>   "+line)
       # outfileWriter.write(line + "\n")
    else:
        i = 0
        j = 0
        print("in links:")
        for l in filteredInLinks:
            if i < sitesperinstance_in:
                links = str(l)
                if links.startswith("//"):
                    links = links[2:]
                print(links)
                outfileWriter.write(links + "\n")
            else:
                break
            i = i + 1
        print("out links:")
        for l in filteredOutLinks:
            if j < sitesperinstance_out:
                links = str(l)
                if links.startswith("//"):
                    links = links[2:]
                print(links)
                outfileWriter.write(links + "\n")
            else:
                break
            j = j + 1
    outfileWriter.close()



# alllinks= getAllExternalLinks("http://www.leboncoin.fr")
#
# internal, external =getinternalLinks(alllinks,"http://www.leboncoin.fr")
# for l in internal:
#     links=str(l)
#     if links.startswith("//"):
#         links=links[2:]
#
#     print(links)
# print("external")
# for l in external:
#     # links=str(l)
#     # if
#     print(l)


    # filteredInLinks,filteredOutLinks=getinternalLinks(alllinks,"http://www.gala.fr")
    # outfileWriter=open("tmp.txt","a")
    # if len(alllinks)==0:
    #     pass
    # else:

    #     i=0
#     j=0
#     print("in links:")
#     for link in filteredInLinks:
#         if i < sitesperinstance_in:
#             print(link)
#             #outfileWriter.write(link + "\n")
#         else:
#             break
#         i=i+1
#     print("out links:")
#     for link in filteredOutLinks:
#         if j < sitesperinstance_out:
#             print(link)
#             #outfileWriter.write(link + "\n")
#         else:
#             break
#         j = j+ 1
# outfileWriter.close()