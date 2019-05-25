module rosetta {
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;

    //todo these dependencies need to declare a module name, or add a module info file
    requires poi;
    requires poi.ooxml;
    requires poi.ooxml.schemas;
}
