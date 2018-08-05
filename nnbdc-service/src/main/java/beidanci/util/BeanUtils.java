package beidanci.util;

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Id;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.esotericsoftware.reflectasm.MethodAccess;

import beidanci.po.Po;
import beidanci.vo.PagedResults;
import beidanci.vo.Result;
import beidanci.vo.Vo;

/**
 * 本类用于把<code>Po</code>转换为<code>Vo</code>
 *
 * @author MaYubing
 */
public class BeanUtils {
	private static final Logger log = LoggerFactory.getLogger(BeanUtils.class);

	@SuppressWarnings("rawtypes")
	private static Map<Class, MethodAccess> methodMap = new ConcurrentHashMap<Class, MethodAccess>();

	private static Map<String, Integer> methodIndexMap = new ConcurrentHashMap<String, Integer>();

	@SuppressWarnings("rawtypes")
	private static Map<Class, List<Field>> fieldMap = new ConcurrentHashMap<Class, List<Field>>();

	/**
	 * 函数调用深度（用于检测无穷递归）
	 */
	private static ThreadLocal<Integer> callDeep = new ThreadLocal<Integer>();
	private static Integer maxCallDeep = 50;// 最大允许函数调用深度

	/**
	 * 由持久对象（PO）生成值对象（VO）。VO各字段值由PO同名字段复制而来，如果某字段本身也是VO，在复制过程中会由PO自动转换为VO。<br>
	 * 本方法会递归转换子孙对象。<br>
	 * 可以指定哪些字段（可以含VO类名）不需要复制，这样可以避免不必要的性能损失，也可以有效防止转换过程出现无穷递归。
	 *
	 * @param po
	 * @param voClass
	 * @return
	 */
	public static <T extends Vo> T makeVO(Po po, Class<T> voClass, String[] excludeFields) {
		HashSet<String> excludes = parseExcludeFields(excludeFields);
		callDeep.set(0);
		T vo = makeVO(po, voClass, excludes);
		assert (callDeep.get().equals(0));
		return vo;
	}

	public static <T extends Vo> List<T> makeVos(List<? extends Po> pos, Class<T> voClass, String[] excludeFields) {
		List<T> vos = new ArrayList<T>(100);
		for (Po po : pos) {
			T vo = makeVO(po, voClass, excludeFields);
			vos.add(vo);
		}
		return vos;
	}

	public static <T extends Vo> PagedResults<T> makePagedVos(PagedResults<? extends Po> pos, Class<T> voClass,
			String[] excludeFields) {
		List<T> vos = makeVos(pos.getRows(), voClass, excludeFields);
		PagedResults<T> pagedVos = new PagedResults<T>(pos.getTotal(), vos);
		return pagedVos;
	}

	public static <T extends Vo> Result<T> makeVoResult(Result<? extends Po> poResult, Class<T> voClass,
			String[] excludeFields) {
		T vo = makeVO(poResult.getData(), voClass, excludeFields);
		return new Result<T>(poResult.isSuccess(), poResult.getMsg(), vo);
	}

	public static <T extends Vo> Result<List<T>> makeVosResult(Result<List<? extends Po>> posResult, Class<T> voClass,
			String[] excludeFields) {
		List<T> vos = BeanUtils.makeVos(posResult.getData(), voClass, excludeFields);
		Result<List<T>> result2 = new Result<List<T>>(posResult.isSuccess(), posResult.getMsg(), vos);
		return result2;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <T extends Vo> T makeVO(Po po, Class<T> voClass, HashSet<String> excludeFields) {
		if (po == null) {
			return null;
		}

		// 进入函数，增加调用深度
		callDeep.set(callDeep.get() + 1);
		if (callDeep.get() > maxCallDeep) {
			throw new RuntimeException("函数调用深度大于" + maxCallDeep);
		}

		T vo;
		try {
			vo = voClass.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		MethodAccess destMethodAccess = methodMap.get(voClass);
		if (destMethodAccess == null) {
			destMethodAccess = cache(vo);
		}

		// 由于PO对象可能为动态包装的Proxy(Hibernate会把lazy load的属性包装为Proxy)，所以需要特殊处理
		String poClassName = po.getClass().getName();
		int suffix = poClassName.indexOf("_$$");
		if (suffix != -1) {
			po = initializeAndUnproxy(po);
		}
		MethodAccess srcMethodAccess = methodMap.get(po.getClass());
		if (srcMethodAccess == null) {
			srcMethodAccess = cache(po);
		}

		List<Field> fieldList = fieldMap.get(voClass);
		for (Field field : fieldList) {
			try {
				if (isFieldExcluded(voClass.getSimpleName(), field.getName(), excludeFields)) {
					continue;
				}
				String fieldName = StringUtils.capitalize(field.getName());
				String getKey = po.getClass().getName() + "." + "get" + fieldName;
				String setkey = vo.getClass().getName() + "." + "set" + fieldName;
				Integer getIndex = methodIndexMap.get(getKey);
				if (getIndex != null) {
					int setIndex = methodIndexMap.get(setkey);
					Object srcValue = srcMethodAccess.invoke(po, getIndex);

					Object destValue = srcValue;
					if (Vo.class.isAssignableFrom(field.getType())) {
						if (log.isTraceEnabled()) {
							log.trace(String.format("voClass[%s] fieldName[%s] fieldType[%s]", voClass.getSimpleName(),
									field.getName(), field.getType().getSimpleName()));
						}
						if (srcValue != null && !(srcValue instanceof Po)) {
							throw new RuntimeException(String.format("期望是Po但实际是%s--", srcValue.getClass())
									+ String.format("voClass[%s] fieldName[%s] fieldType[%s]", voClass.getSimpleName(),
											field.getName(), field.getType().getSimpleName()));
						}
						destValue = makeVO((Po) srcValue, (Class<T>) field.getType(), excludeFields);
					} else if (List.class.isAssignableFrom(field.getType())) {
						Type fc = field.getGenericType();
						if (fc instanceof ParameterizedType) {
							ParameterizedType pt = (ParameterizedType) fc;
							Class genericClazz = (Class) pt.getActualTypeArguments()[0];
							if (log.isTraceEnabled()) {
								log.trace(String.format("voClass[%s] fieldName[%s] fieldType[%s] genericClazz[%s]",
										voClass.getSimpleName(), field.getName(), field.getType().getSimpleName(),
										genericClazz.getSimpleName()));
							}
							List<Po> srcList = (List<Po>) srcValue;
							destValue = new ArrayList();
							if (srcList != null) {
								for (Po srcListItem : srcList) {
									((List<Vo>) destValue).add(makeVO(srcListItem, genericClazz, excludeFields));
								}
							}
						}
					}

					destMethodAccess.invoke(vo, setIndex, destValue);
				}
			} catch (RuntimeException e) {
				log.error(String.format("处理%s.%s时发生异常", poClassName, field.getName()));
				throw e;
			}
		}

		// 离开函数，减少调用深度
		callDeep.set(callDeep.get() - 1);
		return vo;
	}

	/**
	 * 利用反射生成指定对象的属性值字符串(只包含有get方法的那些属性)
	 *
	 * @return
	 */
	public static String beanToStr(Object bean) {
		MethodAccess srcMethodAccess = methodMap.get(bean.getClass());
		if (srcMethodAccess == null) {
			srcMethodAccess = cache(bean);
		}

		StringBuilder sb = new StringBuilder();
		List<Field> fieldList = fieldMap.get(bean.getClass());
		for (Field field : fieldList) {
			String fieldName = StringUtils.capitalize(field.getName());
			String getKey = bean.getClass().getName() + "." + "get" + fieldName;
			Integer getIndex = methodIndexMap.get(getKey);
			if (getIndex != null) {
				Object fieldValue = srcMethodAccess.invoke(bean, getIndex);
				sb.append(fieldName);
				sb.append("=");
				sb.append(fieldValue);
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	/**
	 * 获取指定类拥有的属性
	 *
	 * @param clazz
	 * @param recusive
	 *            为true表示也获取继承子祖先类的字段
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static List<Field> getFields(Class clazz, boolean recusive) {
		// 本类的字段
		List<Field> allFields = new ArrayList<Field>();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			allFields.add(field);
		}

		if (recusive) {
			// 父类的字段
			Class superclass = clazz.getSuperclass();
			fields = superclass.getDeclaredFields();
			for (Field field : fields) {
				allFields.add(field);
			}

			// 祖父类的字段
			Class grandclass = superclass.getSuperclass();
			if (grandclass != null) {
				fields = grandclass.getDeclaredFields();
				for (Field field : fields) {
					allFields.add(field);
				}
			}
		}

		return allFields;
	}

	/**
	 * 生成指定对象的MethodAccess并保存在缓存中。
	 *
	 * @return
	 */
	private static MethodAccess cache(Object obj) {
		Class<? extends Object> clazz = obj.getClass();
		synchronized (methodMap) {
			log.info("caching " + clazz.getName());
			MethodAccess methodAccess = MethodAccess.get(clazz);

			// 缓冲字段及其get/set方法
			List<Field> allFields = getFields(clazz, true);
			List<Field> fieldList = new ArrayList<Field>(allFields.size());
			for (Field field : allFields) {
				if (Modifier.isPrivate(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {// 非静态私有变量
					String fieldName = StringUtils.capitalize(field.getName());
					int getIndex = methodAccess.getIndex("get" + fieldName);
					int setIndex = methodAccess.getIndex("set" + fieldName);
					methodIndexMap.put(clazz.getName() + "." + "get" + fieldName, getIndex);
					methodIndexMap.put(clazz.getName() + "." + "set" + fieldName, setIndex);
					fieldList.add(field);
				}
			}
			fieldMap.put(clazz, fieldList);

			// 缓冲get方法(不要求有相应的字段成员)
			for (Method method : clazz.getMethods()) {
				String methodName = method.getName();
				String cachedName = clazz.getName() + "." + methodName;
				if (methodName.startsWith("get") && !methodName.equals("getClass")
						&& !methodIndexMap.containsKey(cachedName)) {
					int getIndex = methodAccess.getIndex(methodName);
					methodIndexMap.put(cachedName, getIndex);
				}
			}

			methodMap.put(clazz, methodAccess);
			return methodAccess;
		}
	}

	/**
	 * 把Hibernate代理对象还原为源对象
	 *
	 * @param entity
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T initializeAndUnproxy(T entity) {
		if (entity == null) {
			throw new NullPointerException("Entity passed for initialization is null");
		}

		Hibernate.initialize(entity);
		if (entity instanceof HibernateProxy) {
			entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
		}
		return entity;
	}

	/**
	 * 判断指定类的指定字段是否应该被排除
	 *
	 * @param className
	 * @param fieldName
	 * @param excludeFields
	 * @return
	 */
	private static boolean isFieldExcluded(String className, String fieldName, HashSet<String> excludeFields) {
		return excludeFields.contains(new StringBuilder("Any.").append(fieldName).toString())
				|| excludeFields.contains(new StringBuilder(className).append(".").append(fieldName).toString());
	}

	/**
	 * 把PO转换为VO的过程中，需要排除某些字段，这些字段以字符串数组的形式传入,本函数解析该数组，并转为HashSet，便于程序使用。
	 *
	 * @return
	 */
	private static HashSet<String> parseExcludeFields(String[] excludeFields) {
		HashSet<String> fields = new HashSet<String>();
		if (excludeFields == null) {
			return fields;
		}

		for (String excludeField : excludeFields) {
			// 解析类名和字段
			String[] classNameAndFields = excludeField.split("\\.");
			String className = null; // Any指“任何类”
			String[] fieldsOfClass = null;
			if (classNameAndFields.length == 1) {
				className = "Any"; // Any指“任何类”
				fieldsOfClass = classNameAndFields[0].split(",");
			} else if (classNameAndFields.length == 2) {
				className = classNameAndFields[0];
				fieldsOfClass = classNameAndFields[1].split(",");
			} else {
				assert (false);
			}

			// 把要排除的字段（连同类名）逐个加入到HashSet中
			for (String field : fieldsOfClass) {
				fields.add(new StringBuilder(className).append(".").append(field).toString());
			}
		}
		return fields;
	}

	/**
	 * 把对象的所有属性设为null, 除了指定的那些属性
	 *
	 * @param object
	 * @param exclude
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("rawtypes")
	public static void setPropertiesToNull(Object object, String[] excludeFields)
			throws IllegalArgumentException, IllegalAccessException {
		if (object == null) {
			return;
		}
		HashSet<String> excludes = new HashSet<String>();
		for (String field : excludeFields) {
			excludes.add(field);
		}
		Class clazz = object.getClass();
		for (Field field : clazz.getDeclaredFields()) {
			if (!excludes.contains(field.getName()) && !((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC)) {
				field.setAccessible(true);
				field.set(object, null);
			}
		}

		// 父类的字段
		Class superclass = clazz.getSuperclass();
		for (Field field : superclass.getDeclaredFields()) {
			if (!excludes.contains(field.getName()) && !((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC)) {
				field.setAccessible(true);
				field.set(object, null);
			}
		}

		// 祖父类的字段
		Class grandclass = superclass.getSuperclass();
		for (Field field : grandclass.getDeclaredFields()) {
			if (!excludes.contains(field.getName()) && !((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC)) {
				field.setAccessible(true);
				field.set(object, null);
			}
		}
	}

	/**
	 * 利用反射获取指定PO对象的ID值
	 *
	 * @param po
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static Serializable getIdOfPo(Po po) throws IllegalArgumentException, IllegalAccessException {
		List<Field> fieldList = fieldMap.get(po.getClass());
		if (fieldList == null) {
			cache(po);
			fieldList = fieldMap.get(po.getClass());
			assert (fieldList != null);
		}
		for (Field field : fieldList) {
			if (field.isAnnotationPresent(Id.class)) {
				field.setAccessible(true);
				return (Serializable) field.get(po);
			}
		}
		throw new RuntimeException("指定PO对象没有定义ID字段");
	}

}