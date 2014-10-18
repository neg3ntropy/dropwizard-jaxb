package io.dropwizard.jaxb;

import io.dropwizard.validation.ConstraintViolations;
import io.dropwizard.validation.Validated;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.groups.Default;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import javax.xml.parsers.SAXParserFactory;

import com.sun.jersey.core.impl.provider.entity.XMLRootElementProvider;
import com.sun.jersey.spi.inject.Injectable;

public abstract class JaxbXMLValidatingMessageBodyProvider implements MessageBodyReader<Object>,
    MessageBodyWriter<Object> {

  private final Validator validator;
  private final XMLRootElementProvider wrapped;

  protected JaxbXMLValidatingMessageBodyProvider(XMLRootElementProvider wrapped, Validator a) {
    this.wrapped = wrapped;
    this.validator = a;
  }

  @Produces(MediaType.APPLICATION_XML)
  @Consumes(MediaType.APPLICATION_XML)
  public static final class App extends JaxbXMLValidatingMessageBodyProvider {

    public App(@Context Validator validator, @Context Injectable<SAXParserFactory> spf,
        @Context Providers ps) {
      super(new XMLRootElementProvider.App(spf, ps), validator);
    }
  }

  @Produces(MediaType.TEXT_XML)
  @Consumes(MediaType.TEXT_XML)
  public static final class Text extends JaxbXMLValidatingMessageBodyProvider {

    public Text(@Context Validator validator, @Context Injectable<SAXParserFactory> spf,
        @Context Providers ps) {
      super(new XMLRootElementProvider.App(spf, ps), validator);
    }
  }

  @Produces(MediaType.WILDCARD)
  @Consumes(MediaType.WILDCARD)
  public static final class General extends JaxbXMLValidatingMessageBodyProvider {

    public General(@Context Validator validator, @Context Injectable<SAXParserFactory> spf,
        @Context Providers ps) {
      super(new XMLRootElementProvider.App(spf, ps), validator);
    }
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return wrapped.isWriteable(type, genericType, annotations, mediaType);
  }

  @Override
  public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return wrapped.getSize(t, type, genericType, annotations, mediaType);
  }

  @Override
  public void writeTo(Object t, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    wrapped.writeTo(t, type, genericType, annotations, mediaType, httpHeaders, entityStream);
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return wrapped.isReadable(type, genericType, annotations, mediaType);
  }

  @Override
  public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    Object value = wrapped.readFrom(type, genericType, annotations, mediaType, httpHeaders,
        entityStream);
    return validate(annotations, value);
  }

  private Object validate(Annotation[] annotations, Object value) {
    final Class<?>[] classes = findValidationGroups(annotations);
    if (classes != null) {
      final Set<ConstraintViolation<Object>> violations = validator.validate(value, classes);
      if (!violations.isEmpty()) {
        throw new ConstraintViolationException("The request entity had the following errors:",
            ConstraintViolations.copyOf(violations));
      }
    }
    return value;
  }

  private static final Class<?>[] DEFAULT_GROUP_ARRAY = { Default.class };

  private Class<?>[] findValidationGroups(Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      if (annotation.annotationType() == Valid.class) {
        return DEFAULT_GROUP_ARRAY;
      } else if (annotation.annotationType() == Validated.class) {
        return ((Validated) annotation).value();
      }
    }
    return null;
  }

}
