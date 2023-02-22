package com.smactworks.oracle.erp.integration.bean;

import java.io.Serializable;
import java.util.Optional;

import com.opencsv.bean.CsvBindByName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class ControlBean implements Serializable {
  @CsvBindByName(column = "path")
  private Optional<String> path;
  
  @CsvBindByName(column = "fileName")
  private Optional<String> fileName;
  
  @CsvBindByName(column = "processId")
  private Optional<String> processId;
  
  @CsvBindByName(column = "processStatus")
  private Optional<String> processStatus;
  
  @CsvBindByName(column = "fileStamp")
  private Optional<String> fileStamp;
  
  @CsvBindByName(column = "processedTime")
  private Optional<String> processedTime;
  
  
  
  /*public String getFileName() {
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
  }*/
}
