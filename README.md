#   Patchca: verification code library for Java
### Overview
Simple yet powerful verification code library written in Java with zero dependency.
You can generate verification code picture like this:

```xml
  <dependency>
    <groupId>com.bladejava</groupId>
    <artifactId>blade-patchca</artifactId>
    <version>1.0.3</version>
  </dependency>
```

```java
ConfigurableCaptchaService cs = new ConfigurableCaptchaService();
cs.setColorFactory(new SingleColorFactory(new Color(25, 60, 170)));
cs.setFilterFactory(new CurvesRippleFilterFactory(cs.getColorFactory()));

FileOutputStream fos = new FileOutputStream("patcha_demo.png");
EncoderHelper.getChallangeAndWriteImage(cs, "png", fos);
fos.close();
```

it generate picture like this:    
![sample](./demo.png)

