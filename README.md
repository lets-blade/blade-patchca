#   Blade-Patchca: verification code library for Java
### Overview
Simple yet powerful verification code library written in Java with zero dependency.

You can generate verification code picture like this:

![sample](./demo.png)

### Steps to Integrate
- Add following dependency in your pom.xml
```xml
  <dependency>
    <groupId>com.bladejava</groupId>
    <artifactId>blade-patchca</artifactId>
    <version>1.0.3</version>
  </dependency>
```
- Add following dependency in your pom.xml
```java
ConfigurableCaptchaService cs = new ConfigurableCaptchaService();
cs.setColorFactory(new SingleColorFactory(new Color(25, 60, 170)));
cs.setFilterFactory(new CurvesRippleFilterFactory(cs.getColorFactory()));

FileOutputStream fos = new FileOutputStream("demo.png");
String challenge = EncoderHelper.getChallangeAndWriteImage(cs, "png", fos);
//Challenge text needs to be kept in the session for verification 
fos.close();
```
