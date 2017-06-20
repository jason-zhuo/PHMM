# PHMM for WFP
Profile HMM demo code and data

## Dependency
1. clustal-omega-1.2.3
2. argtable2-13
3. hmmer-3.1b2-macosx-intel
4. Java and python environment
5. Jnetpcap 

## Usage

1. Install HMMER 3
2. Install clustal-omega-1.2.3
3. Try data from folder Data using scripts from folder called Shellscript
4. Put the test data and the script in the same folder
4. run ./buildmodel.sh  (it will cost some time)
5. Read result


## Directories

#### C-pcapcrwaler 
This folder contains programs to crawl and collect pcapfiles.

#### Data
This folder contains some key research data for reproducing. 

#### Java
This folder includes pcap file processing code.

#### Python
This folder contains modified KFP algorithm, and sample testing data. And srcipts for analyses.

#### Shellscript
This folder contains shellsripts to analyze gene sequence file data and scripts to parse results. 


## Screenshot

1. PHMM result for Alexa23 L400 after MSA alignment. 

![image](/screenshot/Alexa23.png)

2. PHMM result for Alexa30 L800 after MSA alignment. 

![image](/screenshot/Alexa30.png)

3. PHMM result for Alexa200 L400 after MSA alignment.

![image](/screenshot/home200.png)

4.  Full sequence test result for Alexa30 using model L=800..

![image](/screenshot/Alexa30full.png)



