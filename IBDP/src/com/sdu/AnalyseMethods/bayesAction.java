package com.sdu.AnalyseMethods;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;


import com.sdu.ToolsUse.AdviceHibernate;
import com.sdu.ToolsUse.DataFileHibernate;
import com.sdu.ToolsUse.HDFSTools;
import com.sdu.entity.Admin;
import com.sdu.entity.Advice;
import com.sdu.entity.DataFile;
import com.sdu.entity.Project;

public class bayesAction extends BasicMethod{

	public DataFile beiginAnalyse(int index, DataFile dataFile, Admin user, Project project, JSONObject projectJSON, JSONArray algorithmJSON)
	{
			System.out.println("bayes method");
			//获取贝叶斯算法所需参数
			String filepath=dataFile.getD_localpath();
			String dataFileName=dataFile.getD_name();	
			JSONObject algorithm_obj=algorithmJSON.getJSONObject(index); 
			JSONArray params= algorithm_obj.getJSONArray("param");
			//String hasheader=params.getJSONObject(0).getString("value");
			String formula=params.getJSONObject(0).getString("value");
			
			
	    	System.out.println("链接Rserve，开始分析任务");
	    	DataFile resultFile=new DataFile();//要返回的文件
			try {
			RConnection c= new RConnection();
			
	    	System.out.println("Rserve连接成功");
	    	System.out.println("输入参数为："+filepath+ "  "+dataFileName+"  "+formula);
	    	//String  savePath="/home/jc/IBDP2/"+user.getId()+"/DataFiles";
	    	String savePath=filepath.substring(0,filepath.lastIndexOf('/'));
	    	System.out.println(savePath);
	    	c.eval("setwd(\""+savePath+"\")");
	    	c.eval("library(openxlsx)"); 
	    	String aa = dataFileName.substring(dataFileName.lastIndexOf("."));
	    	
	    	if(aa.equals(".xlsx"))
			  {
	    		c.eval("datafile <- read.xlsx(\""+dataFileName+"\",1)");
			  }else  if (aa.equals(".txt"))
			  {
				  c.eval("datafile <- read.table(\""+dataFileName+"\",header="+dataFile.getD_hasheader()+")");
			
			  }else if (aa.equals(".csv"))
			  {
				  c.eval("datafile<-read.csv(\""+dataFileName+"\",header="+dataFile.getD_hasheader()+",sep=\",\")");
			  }else if(aa.equals(".Rdata"))
			  {
				  c.eval("load(\""+dataFileName+"\")");
			  }
	    		
     	c.eval("library(sparklyr)");
     	c.eval("library(dplyr)");
     	c.eval("library(ggplot2)");
     	//c.eval("Sys.setenv(SPARK_HOME='/usr/hdp/2.4.3.0-227/spark')");
     	//c.eval("Sys.setenv(JAVA_HOME='/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.65-3.b17.el7.x86_64')");
     	

     	//c.eval("Sys.setenv(HADOOP_CONF_DIR='/usr/hdp/current/hadoop-client/conf')");
     	//c.eval("Sys.setenv(YARN_CONF_DIR='/etc/hadoop/2.4.3.0-227/0')");
     	//master = "yarn-client", version="1.6.0", spark_home = '/opt/cloudera/parcels/CDH/lib/spark/'
   
     	c.eval("sc<-spark_connect(master = \"local\" )"); 
     	System.out.println("spark连接成功");
     	c.eval("data_tbl <- copy_to(sc, datafile, \"datafile\", overwrite = TRUE)");
     	System.out.println("model<-ml_naive_bayes(data_tbl,"+formula+")");
     	c.eval("model<-ml_naive_bayes(data_tbl,"+formula+")");
     	c.eval("bayes_predict<-sdf_predict(model,data_tbl)");
	    	
     	String resultFileName=dataFileName.substring(0,dataFileName.lastIndexOf('.'))+"_bayes";
     	System.out.println("开始写入结果文件:"+resultFileName);
     	//假如文件是中间文件的话，文件类别为IntermediateFile，存储为Rdata数据
     	//假如文件是结果文件的话，文件类别为ResultFile，存储为txt数据或者是图片
     	if(index==algorithmJSON.length()-1)
		{
     		resultFileName=resultFileName+".txt";
     	    c.eval("sink(\""+resultFileName+"\")");
			c.eval("print(as.data.frame(bayes_predict))");
			c.eval("sink()");
		}
     	else
     	{
     		resultFileName=resultFileName+".Rdata";
     		c.eval("save(bayes_predict,\""+resultFileName+"\" )");
     	}
     	//c.eval("save(bayes_predict,"+resultFileName+")");
			c.close();
			System.out.println("Rserve连接关闭");
			
			//将结果文件上传到hdfs中
			
			Date date=new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			resultFile.setD_createTime(sdf.format(date));
			resultFile.setD_admin(user);
			resultFile.setD_hasheader("FALSE");
			resultFile.setD_name(resultFileName);
			resultFile.setD_suffix("txt");
			
			resultFile.setD_project(project);
			resultFile.setD_localpath(savePath+"/"+resultFileName);
			File f= new File(savePath+"/"+resultFileName);  
			if (f.exists() && f.isFile())  
			        resultFile.setD_size(f.length()/1024.0+"KB");
			else {
			resultFile.setD_size("Unknown");
			}
			
			if(index==algorithmJSON.length()-1)
			{
				resultFile.setD_type("ResultFile");
				DataFileHibernate.saveDataFile(resultFile);
				HDFSTools.LoadSingleFileToHDFS(resultFile);
				
				Advice advice=new Advice();
				advice.setA_content(project.getP_name()+"分析结果完成");
				advice.setA_isread(false);
				Date date2=new Date();
				advice.setA_time(sdf.format(date2));
				advice.setA_name(project.getP_name()+"分析结果完成");
				advice.setAdvice_admin(user);
				advice.setA_type("AnalyseDone");
				AdviceHibernate.CreateAdvice(advice);
			}
			else {
				resultFile.setD_type("IntermediateFile");
				
			}
			
		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
			return resultFile;
	}
}
