package com.mw.object.search.contributor.tracker;

import com.liferay.object.model.ObjectEntry;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

@Component(immediate = true)
@SuppressWarnings("rawtypes")
public class CustomObjectModelDocumentContributorTracker {
    
    @Activate
    public void activate(BundleContext bundleContext) {
        try {
        	String filterString = String.format(
        		    "(&" +
        		        "(objectClass=%s)" +
        		        "(indexer.class.name=com.liferay.object.model.ObjectDefinition#*)" +
        		        "(!(custom.indexer.class.name=*))" +  // Only match if the property is NOT present. Prevents an infinite loop for the newly registered services...
        		    ")",
        		    ModelDocumentContributor.class.getName()
        		);

            Filter filter = bundleContext.createFilter(filterString);
            
            _log.info("Starting ModelDocumentContributor<ObjectEntry> tracker...");

            _tracker = new ServiceTracker<>(
                bundleContext,
                filter,
                new ServiceTrackerCustomizer<ModelDocumentContributor<ObjectEntry>, ModelDocumentContributor<ObjectEntry>>() {

                    @Override
                    public ModelDocumentContributor<ObjectEntry> addingService(
                        ServiceReference<ModelDocumentContributor<ObjectEntry>> reference) {
                    	
                        ModelDocumentContributor<ObjectEntry> service = bundleContext.getService(reference);
                        
                    	try {
							String indexerClassName = (String)reference.getProperty("indexer.class.name");
							
							if (Validator.isNotNull(indexerClassName)) {
							    ServiceRegistration<ModelDocumentContributor> customServiceRegistration = bundleContext.registerService(
							    		ModelDocumentContributor.class,
							    		new CustomObjectEntryDocumentContributor(), 
							    		HashMapDictionaryBuilder.<String, Object>put("indexer.class.name", indexerClassName).put("custom.indexer.class.name", indexerClassName).build());

							    _customServiceRegistrations.put(indexerClassName, customServiceRegistration);
							    
							    _log.info("Registered CustomObjectEntryDocumentContributor for " + indexerClassName);                       		
							}
						} catch (Exception e) {
							_log.error(e.getClass() + ", " + e.getMessage(), e);
						}        
                        
                        return service;
                    }

                    @Override
                    public void modifiedService(
                        ServiceReference<ModelDocumentContributor<ObjectEntry>> reference,
                        ModelDocumentContributor<ObjectEntry> service) {
                    }

                    @Override
                    public void removedService(
                        ServiceReference<ModelDocumentContributor<ObjectEntry>> reference,
                        ModelDocumentContributor<ObjectEntry> service) {

                        bundleContext.ungetService(reference);
                        
                        try {
							String indexerClassName = (String)reference.getProperty("indexer.class.name");
							
							if (Validator.isNotNull(indexerClassName) && _customServiceRegistrations.containsKey(indexerClassName)) {
								ServiceRegistration<ModelDocumentContributor> customServiceRegistration = _customServiceRegistrations.get(indexerClassName);
								
								customServiceRegistration.unregister();
								
								_customServiceRegistrations.remove(indexerClassName);
								
								_log.info("Unregistered CustomObjectEntryDocumentContributor for " + indexerClassName);
							}
						} catch (Exception e) {
							_log.error(e.getClass() + ", " + e.getMessage(), e);
						}
                    }
                }
            );

            _tracker.open();
            _log.info("Started ModelDocumentContributor<ObjectEntry> tracker...");
        } catch (Exception e) {
            _log.error("Failed to start tracker for ModelDocumentContributor<ObjectEntry>", e);
        }
    }

    @Deactivate
    public void deactivate() {
        if (_tracker != null) {
            _tracker.close();
        }
        
        for (ServiceRegistration<?> registration : _customServiceRegistrations.values()) {
        	 _log.info("deactivate, unregistering: " + registration.getClass().toString());
        	
            registration.unregister();
        }
        
        _customServiceRegistrations.clear();
    }
    
    private ServiceTracker<ModelDocumentContributor<ObjectEntry>, ModelDocumentContributor<ObjectEntry>> _tracker;    
   
	private final Map<String, ServiceRegistration<ModelDocumentContributor>> _customServiceRegistrations = new ConcurrentHashMap<>();    
    
    private static final Log _log = LogFactoryUtil.getLog(CustomObjectModelDocumentContributorTracker.class);
}