# Hello World

Once you have added the PrimeFaces JAR to your classpath, you just need to add the PrimeFaces namespace
to your page to begin using the components. Here is a simple page like `test.xhtml`;

```html
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="jakarta.faces.html"
      xmlns:f="jakarta.faces.core"
      xmlns:faces="jakarta.faces"
      xmlns:ui="jakarta.faces.facelets"
      xmlns:p="http://primefaces.org/ui">

    <f:view contentType="text/html;charset=UTF-8" encoding="UTF-8">
        <h:head>

        </h:head>
        <h:body>
            <p:textEditor />
        </h:body>
    </f:view>

</html>
```

When you run this page through the Faces Servlet mapping (e.g. `*.xhtml`), you should see a rich text editor
when you run the page with `test.xhtml`.