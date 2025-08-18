package com.mw.object.search.contributor.tracker;

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
	
	public interface FIELDS {
		public static final String OBJECT_ENTRY_CONTENT = "objectEntryContent";
	}

	@Override
	public void contribute(Document document, ObjectEntry objectEntry) {
		Map<String, Serializable> objectValues = objectEntry.getValues();

		if (objectValues.containsKey(FIELDS.OBJECT_ENTRY_CONTENT)) {
			String objectEntryContentValue = (String)objectValues.get(FIELDS.OBJECT_ENTRY_CONTENT);
			
			if (Validator.isNotNull(objectEntryContentValue)) {
				document.add(new Field(FIELDS.OBJECT_ENTRY_CONTENT, objectEntryContentValue));
				
				_log.info("Updated objectEntryContent search document field on objectEntryId: " + objectEntry.getObjectEntryId());
			} else {
				_log.info("objectEntryContent field empty for objectEntryId: " + objectEntry.getObjectEntryId() + ", default search document field will be used...");
			}
		} else {
			_log.info("objectEntryContent field not found on objectEntryId: " + objectEntry.getObjectEntryId() + ", default search document field will be used...");
		}
	}
	
	private static Log _log = LogFactoryUtil.getLog(CustomObjectEntryDocumentContributor.class);
}