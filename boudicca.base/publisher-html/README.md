# Boudicca HTML

## Start application
```
gradlew bootRun or ./gradlew bootRun
```

## Stack  
VanillaJS  
VanillaCSS  
Handlebars Java Port  
 
## Noteworthy  
The html client should focus on inline css, keep response time in mind  
Who needs external packages anyways
Extract style configuration `theme.hbs` to maintain some kind of style system  
Do not include any third party hosted packages or libraries (self-host)     
We love inline css and we love inline svg, we love SSR  
Define svgs inside `symbol` tag to enable inline svgs (`svgsprite.hbs`)