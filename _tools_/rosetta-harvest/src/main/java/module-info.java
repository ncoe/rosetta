module rosetta {
    requires java.net.http;

    requires com.fasterxml.jackson.databind;
    requires logstash.logback.encoder;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.eclipse.jgit;

    //todo these dependencies need to declare a module name, or add a module info file
    requires freemarker;
    requires ooxml.schemas; // being used in place of poi.ooxml.schemas because this gives access to CtSortState
    requires poi;
    requires poi.ooxml;
    requires slf4j.api;

    exports com.github.ncoe.rosetta.dto to freemarker;
}
