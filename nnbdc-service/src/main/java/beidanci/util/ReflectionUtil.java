/**
 * 
 */
package beidanci.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Yongrui Wang
 *
 */
public class ReflectionUtil {

	public static Object getFieldValue(Object entity, String fieldName) {
		Object gettedValue = null;
		BeanInfo beanInfo = null;
		PropertyDescriptor[] propertyDescriptors = null;
		try {
			beanInfo = Introspector.getBeanInfo(entity.getClass());
			propertyDescriptors = beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				String displayName = propertyDescriptor.getDisplayName();
				if (displayName.equals(fieldName)) {
					gettedValue = propertyDescriptor.getReadMethod().invoke(entity);
					break;
				} else {
					String[] fieldNameArray = fieldName.split("\\.");
					if (0 != fieldNameArray.length && displayName.equals(fieldNameArray[0])) {
						String str = fieldNameArray[0].toString() + ".";
						String subEntityFieldName = fieldName.replace(str, "");
						Object subEntity = propertyDescriptor.getReadMethod().invoke(entity);
						if (null != subEntity) {
							gettedValue = getFieldValue(subEntity, subEntityFieldName);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return gettedValue;
	}

	public static void setFieldValue(Object entity, String fieldName, Object value)
			throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		BeanInfo beanInfo = null;
		PropertyDescriptor[] propertyDescriptors = null;

		if (null != fieldName) {
			beanInfo = Introspector.getBeanInfo(entity.getClass());
			propertyDescriptors = beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				if (propertyDescriptor.getDisplayName().equals(fieldName)) {
					propertyDescriptor.getWriteMethod().invoke(entity, value);
					break;
				}
			}
		}
	}

}
