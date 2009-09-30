
package org.apache.openejb.jee.was.v6.ecore;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EAttribute complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EAttribute">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.eclipse.org/emf/2002/Ecore}EStructuralFeature">
 *       &lt;attribute name="iD" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EAttribute")
public class EAttribute
    extends EStructuralFeature
{

    @XmlAttribute(name = "iD")
    protected Boolean id;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setID(Boolean value) {
        this.id = value;
    }

}
