# CkService Annotation
- servicegroup.xml 에 명시하지 않아도 class 에 com.tmax.proobject.common.CkService Annotation 을 달아 빌드시 servicegroup.xml 에 추가하도록 함

#### 준비
1. [proobject7-app-template](https://github.com/whojes/proobject7-app-template) 프로젝트로부터 만들어진 proobject 프로젝트에만 적용이 가능하다.
2. 해당 프로젝트의 빌드 결과물을 proobject app 프로젝트에 가져다놓는다.
   - `./gradlew build` 후 `build/libs/custom-ap-local.jar` 파일

#### 사용 방법

1. application 혹은 servicegroup 의 build.gradle 내부 dependency 블록에 다음과 같은 두 줄을 추가한다. 
```gradle
// 생략
dependencies {
    //생략 
	annotationProcessor files("${rootProject.projectDir.toString()}/libs/custom-ap-local.jar")
	implementation files("${rootProject.projectDir.toString()}/libs/custom-ap-local.jar")
}
//후략
```

2. servicegroup.xml 에 다음과 같이 `<!-- INSERT HERE -->`를 정확하게 입력한다.
  - 이 String 을 마커 삼아서 이 뒤에 작성하니까.
  - 예시
    ```xml
      <!-- 생략 -->
      <!-- Annotation 을 달거나 여기에 적거나 둘중 하나만 해야함 -->
      <ns17:service-object>
        <ns17:name>Hello-worldsDelete</ns17:name>
        <ns17:class-name>com.tmax.poapp.poservicegroup.service.HelloWorldDelete</ns17:class-name>
        <ns17:input-dto>com.tmax.poapp.dataobject.potest.HelloWorldIn</ns17:input-dto>
        <ns17:output-dto>com.tmax.poapp.dataobject.potest.HelloWorldOut</ns17:output-dto>
        <ns17:service-type>COMPLEX</ns17:service-type>
      </ns17:service-object>

      <!-- INSERT HERE -->
    </ns17:service-group>
    ```

3. CkServiceObject 를 상속하는 클래스에 다음과 같은 annotation 을 달아버린다.
```java
import com.tmax.proobject.common.CkService;
import com.tmax.proobject.common.HttpMethod;

@CkService(serviceName="hello-worlds", method=HttpMethod.POST)
public class HelloWorldCreate extends CkServiceObject<HelloWorldIn, HelloWorldOut> {
  // service 이름이 Hello-worldsCreate 일 경우
}
```

4. Service Executor 클래스를 만들기 귀찮은 경우, 다음과 같은 annotation 을 추가해도 좋다.
Executor 파일이 빌드 시 생성되어 jar 파일에 포함된 후 삭제되어 파일시스템에는 남지 않는다.
```java
import com.tmax.proobject.common.CkService;
import com.tmax.proobject.common.CkServiceExecutor;
import com.tmax.proobject.common.HttpMethod;

@CkService(serviceName="hello-worlds", method=HttpMethod.POST)
@CkServiceExecutor
public class HelloWorldCreate extends CkServiceObject<HelloWorldIn, HelloWorldOut> {
  // service 이름이 Hello-worldsCreate 일 경우
}

```