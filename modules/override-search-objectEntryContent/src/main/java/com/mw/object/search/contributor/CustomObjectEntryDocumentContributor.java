package com.mw.object.search.contributor;

import com.liferay.object.model.ObjectEntry;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;

import java.io.Serializable;
import java.util.Map;

public class CustomObjectEntryDocumentContributor implements ModelDocumentContributor<ObjectEntry> {

	@Override
	public void contribute(Document document, ObjectEntry objectEntry) {
		
		_log.info("START >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		Map<String, Serializable> ojectValues = objectEntry.getValues();

		if (ojectValues.containsKey("objectEntryContent")) {
			String objectEntryContentValue = (String)ojectValues.get("objectEntryContent");
			
			if (Validator.isNotNull(objectEntryContentValue)) {
				document.add(new Field("objectEntryContent", objectEntryContentValue));
				
				_log.info("Updated objectEntryContent field on objectEntryId: " + objectEntry.getObjectEntryId());
			} else {
				_log.info("objectEntryContent field empty for objectEntryId: " + objectEntry.getObjectEntryId() + ", default field value will be used...");
			}
		} else {
			_log.info("objectEntryContent field not found on objectEntryId: " + objectEntry.getObjectEntryId() + ", default field value will be used...");
		}
		
		_log.info("END >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
	}
	
	private static Log _log = LogFactoryUtil.getLog(CustomObjectEntryDocumentContributor.class);
}