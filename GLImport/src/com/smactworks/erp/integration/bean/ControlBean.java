package com.smactworks.erp.integration.bean;

import com.opencsv.bean.CsvBindByName;
import java.io.Serializable;

public class ControlBean implements Serializable {
  @CsvBindByName(column = "Path")
  private String path;
  
  @CsvBindByName(column = "fileName")
  private String fileName;
  
  @CsvBindByName(column = "processId")
  private String processId;
  
  @CsvBindByName(column = "processStatus")
  private String processStatus;
  
  @CsvBindByName(column = "processedTime")
  private String processedTime;
  
  public String getFileName() {
    return this.fileName;
  }
  
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
  
  public String getProcessId() {
    return this.processId;
  }
  
  public void setProcessId(String processId) {
    this.processId = processId;
  }
  
  public String getProcessStatus() {
    return this.processStatus;
  }
  
  public void setProcessStatus(String processStatus) {
    this.processStatus = processStatus;
  }
  
  public String getProcessedTime() {
    return this.processedTime;
  }
  
  public void setProcessedTime(String processedTime) {
    this.processedTime = processedTime;
  }
  
  public String getPath() {
    return this.path;
  }
  
  public void setPath(String path) {
    this.path = path;
  }
  
  public String toString() {
    return "Control Data [Path=" + this.path + ", FileName=" + this.fileName + ", ProcessId=" + this.processId + ",ProcessStatus= " + this.processStatus + 
      ",ProcessedTime = " + this.processedTime + "]";
  }
}
