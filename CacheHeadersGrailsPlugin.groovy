import groovy.util.ConfigObject

class CacheHeadersGrailsPlugin {
    def version = "1.1.6"
    def grailsVersion = "1.2.0 > *"
    def dependsOn = ['controllers':'1.1 > *']
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "grails-app/controllers/**"
    ]
    
    def author = "Graeme Rocher"
    def authorEmail = "grocher@gopivotal.com"
    def title = "Caching Headers Plugin"
    def description = '''\\
Improve your application performance with browser caching, with easy ways to set caching headers in controller responses 
'''

    def developers = [ [ name: "Marc Palmer", email: "marc@grailsrocks.com" ], [ name: "Graeme Rocher", email: "grocher@gopivotal.com" ]]
    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPCACHEHEADERS" ]
    def scm = [ url: "http://github.com/grails-plugins/grails-cache-headers" ]
    def license = "APACHE"

    def observe = ['controllers']
    
    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/cache-headers"

    def doWithWebDescriptor = { xml ->
    }

    def doWithSpring = {
    }

    def doWithDynamicMethods = { ctx ->
        addCacheMethods(application, log)    
    }

    void reloadConfig(application, svc, log) {
        def conf = application.config.cache.headers
        def cacheSetting = conf.enabled
        svc.enabled = ((cacheSetting instanceof String) || (cacheSetting instanceof Boolean)) ? Boolean.valueOf(cacheSetting.toString()) : true
        svc.presets = conf.presets
        log.info "Caching enabled in Config: ${svc.enabled}"
        log.debug "Caching presets declared: ${svc.presets}"
    }

    void addCacheMethods(application, log) {
        
        def svc = application.mainContext.cacheHeadersService

        application.controllerClasses*.clazz.each { cls ->
 
            if (log.debugEnabled) {
                log.debug "Adding cache methods to ${cls}"
            }
            
            cls.metaClass.cache = { Boolean allow -> svc.cache(delegate.response, allow) }
            
            cls.metaClass.cache << { String preset -> svc.cache(delegate.response, preset) }
            
            cls.metaClass.cache << { Map args -> svc.cache(delegate.response, args) }
            
            cls.metaClass.withCacheHeaders = { Closure c ->
                svc.withCacheHeaders(delegate, c)
            }

            cls.metaClass.lastModified = { dateOrLong -> svc.lastModified(delegate.response, dateOrLong) }
        }        
    }
    
    def doWithApplicationContext = { applicationContext ->
        def svc = applicationContext.cacheHeadersService
        reloadConfig(application, svc, log)
    }

    def onChange = { event ->
        addCacheMethods(event.application, log)    
    }

    def onConfigChange = { event ->
        // Config change might mean that the caching has been turned on/off
        def svc = event.application.mainContext.cacheHeadersService
        reloadConfig(event.application, svc, log)
    }
}
