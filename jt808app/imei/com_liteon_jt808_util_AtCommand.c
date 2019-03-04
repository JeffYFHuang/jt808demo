#include <jni.h>
#include <stdio.h>
#include "AtCommand.h"
#include "ql_oe.h"


#define QUEC_AT_PORT    "/dev/smd8"
static int smd_fd = -1;
int Ql_SendAT(char* atCmd, char* finalRsp, char* rsp ,long timeout_ms);

JNIEXPORT jint JNICALL Java_com_liteon_jt808_util_AtCommand_Ql_1SendAT
  (JNIEnv *env, jobject thisObj, jstring atcmdstr, jstring finalRspstr, jcharArray strResponse, jlong timeout_ms) {
   // Step 1: Convert the JNI String (jstring) into C-String (char*)
   const char *atCmd = (*env)->GetStringUTFChars(env, atcmdstr, NULL);
   if (NULL == atCmd) return -1;

   const char *finalRsp = (*env)->GetStringUTFChars(env, finalRspstr, NULL);
   if (NULL == finalRsp) return -1;
 
   // Step 2: Perform its intended operations
   printf("In C, atcmd is: %s\n", atCmd);
 
   // Prompt user for a C-string

   jchar* array = (*env)->GetCharArrayElements(env, strResponse, 0);
   jsize arraysize = (*env)->GetArrayLength(env, strResponse);

   smd_fd = open(QUEC_AT_PORT, O_RDWR | O_NONBLOCK | O_NOCTTY);
   printf("< open(\"%s\")=%d >\n", QUEC_AT_PORT, smd_fd);

   char rsp[128];
   jint ret = Ql_SendAT(atCmd, finalRsp, rsp, timeout_ms);
   int i = 0;
   for (i = 0; i < strlen(rsp); i++) 
       array[i] = rsp[i];
   (*env)->ReleaseCharArrayElements(env, strResponse, array, JNI_COMMIT);
   // start to send AT...
   close(smd_fd);
 
   return ret; 
}

int Ql_SendAT(char* atCmd, char* finalRsp, char* strResponse, long timeout_ms)
{
    int iRet;
    int iLen;
    fd_set fds;
    int rdLen;
#define lenToRead 100
    char strAT[100];
    char strFinalRsp[100];
//    char strResponse[100];
    struct timeval timeout = {0, 0};
    boolean bRcvFinalRsp = FALSE;

    memset(strAT, 0x0, sizeof(strAT));
    iLen = sizeof(atCmd);
    strncpy(strAT, atCmd, iLen);

    sprintf(strFinalRsp, "\r\n%s", finalRsp);
    
	timeout.tv_sec  = timeout_ms / 1000;
	timeout.tv_usec = timeout_ms % 1000;
    
    
    // Add <cr><lf> if needed
    iLen = strlen(atCmd);
    if ((atCmd[iLen-1] != '\r') && (atCmd[iLen-1] != '\n'))
    {
        iLen = sprintf(strAT, "%s\r\n", atCmd); 
        strAT[iLen] = 0;
    }

    // Send AT
    iRet = write(smd_fd, strAT, iLen);
    printf(">>Send AT: \"%s\", iRet=%d\n", atCmd, iRet);
    // Wait for the response
	while (1)
	{
		FD_ZERO(&fds); 
		FD_SET(smd_fd, &fds); 

		printf("timeout.tv_sec=%d, timeout.tv_usec: %d \n", (int)timeout.tv_sec, (int)timeout.tv_usec);
		switch (select(smd_fd + 1, &fds, NULL, NULL, &timeout))
		//switch (select(smd_fd + 1, &fds, NULL, NULL, NULL))	// block mode
		{
		case -1: 
			printf("< select error >\n");
			return -1;

		case 0:
			printf("< time out >\n");
			return 1; 

		default: 
			if (FD_ISSET(smd_fd, &fds)) 
			{
				do {
					memset(strResponse, 0x0, sizeof(strResponse));
					rdLen = read(smd_fd, strResponse, lenToRead);
					printf(">>Read response/urc, len=%d, content:\n%s\n", rdLen, strResponse);
					//printf("rcv:%s", strResponse);
					//printf("final rsp:%s", strFinalRsp);
					if ((rdLen > 0) && strstr(strResponse, strFinalRsp))
					{
					    if (strstr(strResponse, strFinalRsp)     // final OK response
					       || strstr(strResponse, "+CME ERROR:") // +CME ERROR
					       || strstr(strResponse, "+CMS ERROR:") // +CMS ERROR
					       || strstr(strResponse, "ERROR"))      // Unknown ERROR
					    {
					        //printf("\n< match >\n");
					        bRcvFinalRsp = TRUE;
					    }else{
					        printf("\n< not final rsp >\n");
					    }
					}
				} while ((rdLen > 0) && (lenToRead == rdLen));
			}else{
				printf("FD is missed\n");
			}
			break;
		}

		// Found the final response , return back
		if (bRcvFinalRsp)
		{
		    break;
		}	
   	}
   	return 0;
}
