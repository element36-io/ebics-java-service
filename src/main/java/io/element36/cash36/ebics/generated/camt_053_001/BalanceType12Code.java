//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.05.06 at 12:19:39 PM CEST 
//


package io.element36.cash36.ebics.generated.camt_053_001;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BalanceType12Code.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="BalanceType12Code"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="XPCD"/&gt;
 *     &lt;enumeration value="OPAV"/&gt;
 *     &lt;enumeration value="ITAV"/&gt;
 *     &lt;enumeration value="CLAV"/&gt;
 *     &lt;enumeration value="FWAV"/&gt;
 *     &lt;enumeration value="CLBD"/&gt;
 *     &lt;enumeration value="ITBD"/&gt;
 *     &lt;enumeration value="OPBD"/&gt;
 *     &lt;enumeration value="PRCD"/&gt;
 *     &lt;enumeration value="INFO"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "BalanceType12Code")
@XmlEnum
public enum BalanceType12Code {

    XPCD,
    OPAV,
    ITAV,
    CLAV,
    FWAV,
    CLBD,
    ITBD,
    OPBD,
    PRCD,
    INFO;

    public String value() {
        return name();
    }

    public static BalanceType12Code fromValue(String v) {
        return valueOf(v);
    }

}
