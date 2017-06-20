__author__ = 'zhuozhongliu'

from urllib import *
import urllib2
from bs4 import BeautifulSoup
import tldextract
import re
import random
import datetime

import zlib


pages = set()
random.seed(datetime.datetime.now())



def getAllExternalLinks(siteUrl):
    allExtLinks = set()
    allIntLinks = set()
    alllinks=set()
    # html = urlopen(siteUrl)
    # bsObj = BeautifulSoup(html, 'html.parser')
    #####

    request = urllib2.Request(siteUrl)
    request.add_header('User-agent','firefox')
    opener = urllib2.build_opener()
    try:
        response = opener.open(request)
    except urllib2.HTTPError, e:
        return alllinks
    html = response.read()
    gzipped = response.headers.get('Content-Encoding')
    if gzipped:
        html = zlib.decompress(html, 16 + zlib.MAX_WBITS)
    #print(html)
    bsObj = BeautifulSoup(html, 'html.parser', from_encoding="utf-8")
    #print(bsObj.prettify())
    #####
    url=splitAddress(siteUrl)[0]
    mydomain = tldextract.extract(url)[1] + "." + tldextract.extract(url)[2]
    internalLinks = getInternaLinks(bsObj,mydomain)
    externalLinks = getExternalLinks(bsObj, mydomain)

    site = splitAddress(siteUrl)[0]
    print(site)

    for link in externalLinks:
        if link not in allExtLinks:
            allExtLinks.add(link)
            alllinks.add(link)
            #print('external links:'+link)
    for link in internalLinks:
        if link not in allIntLinks:
            #print("interanl links:" + link)
            allIntLinks.add(link)
            alllinks.add(link)
    #         #getAllExternalLinks(link)
    return alllinks


def getInternaLinks(bsObj,includeUrl):
    internalLinks = []
    #print(bsObj)
    relativePath=True
    allinterallinks = bsObj.findAll('a',href = re.compile("^(\\/\w+)"))
    if len(allinterallinks)==0:
        relativePath = False
        temp = bsObj.findAll('a', href=re.compile('((?='+includeUrl+'))'))
        for i in temp:
            allinterallinks.append(i)
    for link in allinterallinks:
       # print(link)
        if link.attrs['href'] is not None:
            if link.attrs['href'] not in internalLinks:
                if relativePath==True:
                    internalLinks.append(includeUrl+link.attrs['href'])
                else:
                    internalLinks.append(link.attrs['href'])
    return internalLinks

def getExternalLinks(bsObj,excludeUrl):
    externalLinks = []
    for link in bsObj.findAll('a',href = re.compile('^(https|http|www)((?!'+excludeUrl+').)*$')):
        if link.attrs['href'] is not None:
            if link.attrs['href'] not in externalLinks:
                externalLinks.append(link.attrs['href'])
    return externalLinks

def splitAddress(address):
    addressParts = address.replace("http://","").split("/")
    return addressParts

def getRandomExternalLink(startingPage):
    html = urlopen(startingPage)
    bsObj = BeautifulSoup(html,'html.parser')
    externalLinks = getExternalLinks(bsObj,splitAddress(startingPage)[0])
    if len(externalLinks) == 0:
        internalLinks = getInternaLinks(bsObj,startingPage)
        return getInternaLinks(internalLinks[random.randint(0,len(internalLinks)-1)])
    else:
        return externalLinks[random.randint(0, len(externalLinks) - 1)]

def followExternalOnly(startingSite):
    externalLink = getRandomExternalLink(startingSite)
    print("external links:"+externalLink)
    followExternalOnly(externalLink)

# followExternalOnly("http://oreilly.com/")
#getInternaLinks()

