dropwizard-jaxb
===============

Support for XML entity validation in Dropwizard.

Register like this:

    @Override
    public void run(MyConfiguration conf, Environment env) throws Exception {
        JerseyEnvironment jersey = environment.jersey();
        jersey.register(new ValidatorProvider(env.getValidator()));
        jersey.register(JaxbXMLValidatingMessageBodyProvider.App.class);
