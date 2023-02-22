package com.smactworks.oracle.erp.integration.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.Optional;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smactworks.erp.integration.exception.AuthorizationException;
import com.smactworks.erp.integration.exception.ServiceUnavailableException;
import com.smactworks.oracle.erp.integration.Import;
import com.smactworks.oracle.erp.integration.bean.ControlBean;
import com.smactworks.oracle.erp.integration.rest.client.RestClient;
import com.smactworks.oracle.erp.integration.util.Base64Utils;
import com.smactworks.oracle.erp.integration.util.ConfigReader;
import com.smactworks.oracle.erp.integration.util.Constants;
import com.smactworks.oracle.erp.integration.util.ControlFileUtil;
import com.smactworks.oracle.erp.integration.util.FileUtils;

public class GLImport implements Import {
	private String ESS_LOG_FILE_DOWNLOAD_PATH;

	public Long uploadToUCM(String fileName) throws UnsupportedCharsetException, JSONException, ClientProtocolException,
			IOException, AuthorizationException,ServiceUnavailableException {
		ObjectNode node = new ObjectMapper().createObjectNode();
		node.put("OperationName", "uploadFileToUCM").put("DocumentContent", Base64Utils.encodeBase64(fileName))
				.put("DocumentAccount", "fin$/journal$/import$").put("ContentType", Constants.GL_UCM_UPLOAD_CONTENT_TYPE)
				.put("FileName", (new File(fileName)).getName()).put(Constants.UCM_UPLOAD_RESPONSE_DOC_ID, "");
		return new RestClient().post(new StringEntity(node.toString(), ContentType.APPLICATION_JSON))
				.getLong(Constants.UCM_UPLOAD_RESPONSE_DOC_ID);
	}

	public Long submitESSJobRequest(Long documentId, String JobName, String fileName)
			throws UnsupportedCharsetException, ClientProtocolException, IOException, AuthorizationException,ServiceUnavailableException {
		return new RestClient().post(new StringEntity(prepareJobPayload(documentId, JobName, fileName).toString(),
				ContentType.APPLICATION_JSON)).getLong(Constants.ESS_JOB_RESPONSE_PROCESS_ID);
	}

	private ObjectNode prepareJobPayload(Long documentId, String JobName, String fileName) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		if (JobName.equals("InterfaceLoaderController")) {
			node.put("OperationName", "submitESSJobRequest")
					.put("JobPackageName", "oracle/apps/ess/financials/commonModules/shared/common/interfaceLoader")
					.put("JobDefName", "InterfaceLoaderController").put(Constants.UCM_UPLOAD_RESPONSE_DOC_ID, documentId)
					.put("ESSParameters", "15," + documentId + ",N,N," + fileName).put(Constants.ESS_JOB_RESPONSE_PROCESS_ID, "");
		} else if (JobName.equals("JournalImportLauncher")) {
			node.put("OperationName", "submitESSJobRequest")
					.put("JobPackageName", "/oracle/apps/ess/financials/generalLedger/programs/common")
					.put("JobDefName", "JournalImportLauncher");
			String[] tokens = fileName.split("_");
			Constants.loadLeaders();
			if (ConfigReader.getInstance().getRunEnv().equals(Constants.ESS_PROFILE_UAT)) {
				node.put("ESSParameters", Constants.ESS_PROFILE_UAT_ACC_ID
						+ Constants.UAT_JOURNAL_LEDGER_MAP.get(tokens[1]) + "," + tokens[2] + ",N,N,O");
			} else if (ConfigReader.getInstance().getRunEnv().equals(Constants.ESS_PROFILE_PRD)) {
				node.put("ESSParameters", Constants.ESS_PROFILE_PRD_ACC_ID
						+ Constants.PRD_JOURNAL_LEDGER_MAP.get(tokens[1]) + "," + tokens[2] + ",N,N,O");
			}
		}
		return node;
	}

	public String getESSJobStatus(Long requestId, Long sleepTime) throws ClientProtocolException, IOException {
		boolean isCompleted = false;
		String essJobStatus = "";
		int counter = 0;
		String url = String.valueOf(ConfigReader.getInstance().getHostName().trim())
				+ "/fscmRestApi/resources/latest/erpintegrations?finder=ESSJobStatusRF;requestId=" + requestId;
		while (!isCompleted) {
			try {
				Thread.sleep(sleepTime.longValue());
			} catch (InterruptedException interruptedException) {
			}
			JSONObject responseJSON = (new RestClient()).get(url);
			JSONArray items = responseJSON.getJSONArray(Constants.ESS_JOB_PARAM_ITEMS);
			JSONObject item = items.getJSONObject(0);
			String requestStatus = item.getString(Constants.ESS_JOB_RESPONSE_REQUEST_STATUS);
			counter++;
			if (requestStatus.equals(Constants.ESS_JOB_STATUS_SUCCESS) || requestStatus.equals(Constants.ESS_JOB_STATUS_FAILURE)) {
				essJobStatus = requestStatus;
				isCompleted = true;
			}
			if (counter > 4)
				isCompleted = true;
		}
		return essJobStatus;
	}

	public List<Long> getESSJobChildProcessIds(Long parentRequestId) throws ClientProtocolException, IOException {
		String url = String.valueOf(ConfigReader.getInstance().getHostName().trim())
				+ "/fscmRestApi/resources/latest/erpintegrations?finder=ESSJobExecutionDetailsRF;requestId="
				+ parentRequestId + ",fileType=log";
		JSONObject responseJSON = (new RestClient()).get(url);
		JSONArray items = responseJSON.getJSONArray(Constants.ESS_JOB_PARAM_ITEMS);
		JSONObject item = items.getJSONObject(0);
		String documentContent = item.getString("DocumentContent");
		List<Long> childProcessIds = FileUtils.getAllESSChildProcessIds(FileUtils.writeToFile(
				Base64Utils.decodeFromBase64(documentContent), parentRequestId, this.ESS_LOG_FILE_DOWNLOAD_PATH));
		return childProcessIds;
	}

	public String getESSJobChildProcessStatus(List<Long> childProcessIds) throws ClientProtocolException, IOException {
		String processStatus = Constants.ESS_JOB_STATUS_SUCCESS;
		for (Long processId : childProcessIds) {
			processStatus = getESSJobStatus(processId, Long.valueOf(10L));
			if (processStatus.equalsIgnoreCase(Constants.ESS_JOB_STATUS_FAILURE))
				return Constants.ESS_JOB_STATUS_FAILURE;
		}
		return processStatus;
	}

	public void writeToControlFile(ControlBean bean) {
		new ControlFileUtil().writeToControlFile(bean);
	}

	public Optional<ControlBean> readFromControlFile(String fileName) {
		return (new ControlFileUtil()).readFromControlFile(fileName);
	}

	public void setESSLogsDownloadPath(String downloadPath) {
		this.ESS_LOG_FILE_DOWNLOAD_PATH = downloadPath;
	}
}
