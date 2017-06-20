#include <stdio.h>
#include <dirent.h>
#include <sys/types.h> 
#include <time.h>  
#include <stdlib.h>


int getTotalline(const char* fileName){
  int linecount = 0 ;  
  char tmpLine[300];
  FILE *fr;
   if((fr =  fopen(fileName, "rt")) == NULL)
    {
      printf("error: can not open file\n");
      exit(0);
    }
    while (fgets(tmpLine, 300, fr) != NULL)  
    {  
        linecount++;
    } 
   return linecount;
}

char * getRandLine(const char* fileName)  
{  
    char * randline =(char*)calloc(300,sizeof(char*));

    srand((unsigned int)time(NULL)) ; 
    int totalLine=getTotalline(fileName);
    //printf("%d\n", totalLine );
    int random_line=rand()%totalLine;
    //printf("%d\n", random_line);
    int current_line=0;
    char line[300];
    FILE *fr;
    if((fr =  fopen(fileName, "rt")) == NULL)
    {
      printf("error: can not open file\n");
      exit(0);
    }
    while (fgets(line, 300, fr) != NULL)  
    {  
        int len=strlen(line);
        line[len-1] = '\0';  
        if (current_line!=random_line)
        {
          current_line++;
          continue;
        }else{
          randline=strcpy(randline,line);
          break;
        }
    }  
    fclose(fr); 
    return randline;
    //printf("%s\n", randline); 
}  


void doCollecting(char * filepath, char * currentdir, char* mainpage){
	char line[100];
  char fname[100]={'\0'};
  char *name=filepath;
	FILE *fr,*f_stream;
	int num=0;
	int round=0,len=0,time=0;
	char tcpdump[200];
	char firefox[300];
	memset(tcpdump,0,sizeof(tcpdump));



	if((fr =  fopen(name, "rt")) == NULL)
	{
		printf("error: can not open file\n");
		exit(0);
	}


       num=0;
	if(fgets(line, 100, fr) != NULL)
	  {
              time=30;
	       len=strlen(line);
           line[len-1] = '\0';  
		for(round=0;round<10;round++){ 
		          
			sprintf(tcpdump,"tcpdump -i enp0s5 tcp and port 1984 and host 146.57.249.110 -w %s/%d-%d.pcap &",currentdir,num,round);
					//printf("%s\n",tcpdump);			
					system(tcpdump);                                       	
				  sprintf(firefox,"firefox -P default %s &",mainpage);  // vist Mainpage first
          printf("%s\n", firefox);
          system(firefox);
          sleep(2);
          memset(firefox,0,sizeof(firefox));
          char *tmp = getRandLine(name);
          //printf("%s\n", name);
          sprintf(firefox,"firefox -P zzl %s &",tmp);  // vist second random page
          printf("%s\n", firefox);
           
					 system(firefox);		
           sleep(10);  
					 system("pkill tcpdump");
					 system("pkill firefox");
                free(tmp);
				        memset(tcpdump,0,sizeof(tcpdump));
                memset(firefox,0,sizeof(firefox));
			     sleep(15);
		} 
    num++;
}
		fclose(fr);

}

void List(char *path)
{
     printf("[%s]\n", path);
     char childpath[512];
	   char outdirpath[512];
     char mainPage[512];
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
            sprintf(mainPage,"http://%s",ent->d_name);
          	sprintf(outdirpath,"%s%s",path,ent->d_name);
            printf("Main page is %s\n", mainPage);
          	printf("crawling.. -> %s\n",childpath);  
          	doCollecting(childpath,outdirpath,mainPage);
          }
     }
}

int main(int argc, char *argv[])
{
      List(argv[1]);
      // char * ttt[10];
      // for (int i =0; i < 10 ; i++){
      //   srand((unsigned int)time(NULL)) ; 
      //   char *tmp =getRandLine("mini03test/www.360.cn/sitemaps.txt");
      //   printf("%s\n", tmp);
      // }
      //char * tmp =getRandLine("mini03test/www.360.cn/sitemaps.txt");
      //printf("%s\n", tmp);
      return 0;
}
