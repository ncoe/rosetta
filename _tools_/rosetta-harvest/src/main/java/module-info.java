module rosetta {
    requires java.net.http;

    requires freemarker;
    requires logstash.logback.encoder;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.apache.poi.ooxml;
    requires org.apache.poi.poi;
    requires org.eclipse.jgit;
    requires org.slf4j;

    //todo these dependencies need to declare a module name, or add a module info file
    //commons-math3         -> depended on by org.apache.poi:poi
    //SparseBitSet          -> depended on by org.apache.poi:poi

    exports com.github.ncoe.rosetta.dto to freemarker;
}
