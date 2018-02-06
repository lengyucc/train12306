package com.antbean.train12306.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class BeanUtils {

	public static <T> T map2BeanWithViewLike(Map<String, Object> data, Class<T> type)
			throws InstantiationException, IllegalAccessException {
		Field[] declaredFields = type.getDeclaredFields();
		Map<String, Field> map = new HashMap<String, Field>();
		for (Field field : declaredFields) {
			View view = field.getAnnotation(View.class);
			String key = view == null ? field.getName() : view.name();
			map.put(key, field);
		}
		T t = type.newInstance();
		for (Entry<String, Object> entry : data.entrySet()) {
			String view = entry.getKey();
			Object value = entry.getValue();
			Field field = null;

			for (Entry<String, Field> ent : map.entrySet()) {
				if (view.contains(ent.getKey())) {
					field = ent.getValue();
					break;
				}
			}

			if (field != null) {
				field.setAccessible(true);
				field.set(t, value);
			}
		}
		return t;
	}

}
