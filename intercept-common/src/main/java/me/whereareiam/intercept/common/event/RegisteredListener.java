package me.whereareiam.intercept.common.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.whereareiam.intercept.event.EventListener;
import me.whereareiam.intercept.event.base.EventOrder;

import java.lang.reflect.Method;

@Getter
@AllArgsConstructor
public class RegisteredListener {
	private final EventListener listener;
	private final Method method;
	private final EventOrder order;
}