#include <stdio.h>
#include <dirent.h>
#include <sys/types.h>


void doCollecting(char * filepath, char * currentdir){
	char line[300];
       char fname[100]={'\0'};
       char *name=filepath;
	FILE *fr,*f_stream;
	int num=0,lineNum=1;
	int round=0,len=0,time=0;
	char tcpdump[512];
	char firefox[1024];
	memset(tcpdump,0,sizeof(tcpdump));

	if((fr =  fopen(name, "rt")) == NULL)
	{
		printf("error: can not open file\n");
		exit(0);
	}


       num=0;
	while(fgets(line, 300, fr) != NULL)
	  {
              time=30;
	       len=strlen(line);
           line[len-1] = '\0';  
		for(round=0;round<10;round++){       
			sprintf(tcpdump,"tcpdump -i en0 tcp and port 1984 and host 146.57.249.111 -w %s/%d-%d-%d.pcap &",currentdir,num,lineNum,round);
					 printf("%s\n",tcpdump);			
					 system(tcpdump);                                       	
				        sprintf(firefox,"/Applications/Firefox.app/Contents/MacOS/firefox %s &",line);
					printf("%s\n", firefox);
					 system(firefox);		  
					 sleep(20);
					 system("pkill tcpdump");
					 system("pkill firefox");
				        memset(tcpdump,0,sizeof(tcpdump));
			                sleep(time);
		} 
    lineNum++;
    num++;
}
		fclose(fr);




}

void List(char *path)
{
     printf("[%s]\n", path);
     char childpath[512];
	 char outdirpath[512];
     struct dirent* ent = NULL;
     DIR *pDir;
     pDir=opendir(path);
     //d_reclen：16表示子目录或以.开头的隐藏文件，24表示普通文本文件,28为二进制文件，还有其他……
     while (NULL != (ent=readdir(pDir)))
     {
          if(ent->d_type==4){ // subdirs
          	if(strcmp(ent->d_name,".")==0 || strcmp(ent->d_name,"..")==0)
          		continue;
          	sprintf(childpath,"%s%s/%s",path,ent->d_name,"sitemaps.txt");
          	sprintf(outdirpath,"%s%s",path,ent->d_name);
          	printf("crawling.. -> %s\n",childpath);  
          	doCollecting(childpath,outdirpath);
          }
     }
}

int main(int argc, char *argv[])
{
      List(argv[1]);
      return 0;
}
