package io.dropwizard.jaxb;

import java.lang.reflect.Type;

import javax.validation.Validator;
import javax.ws.rs.core.Context;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

public class ValidatorProvider implements Injectable<Validator>, InjectableProvider<Context, Type> {

  private final Validator validator;

  public ValidatorProvider(Validator validator) {
    this.validator = validator;
  }

  @Override
  public Validator getValue() {
    return validator;
  }

  @Override
  public ComponentScope getScope() {
    return ComponentScope.Singleton;
  }

  @Override
  public Injectable<Validator> getInjectable(ComponentContext ic, Context a, Type c) {
    if (Validator.class.equals(c)) {
      return this;
    } else {
      return null;
    }
  }
}