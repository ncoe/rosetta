module rosetta {
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires org.eclipse.jgit;

    //todo these dependencies need to declare a module name, or add a module info file
    requires freemarker;
    requires logstash.logback.encoder;
    requires poi;
    requires poi.ooxml;
    requires poi.ooxml.schemas;
    requires slf4j.api;

    exports com.github.ncoe.rosetta.dto to freemarker;
}
