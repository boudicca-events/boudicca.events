# Boudicca HTML

## Start application

There is the launch config `OnlineHtmlPublisher` you can run/debug.
For template and Javascript/CSS file changes you don't need to rebuild/restart the application.
To reload the application after you made Kotlin changes you can build the project via `Build -> Build Project` or pressing Ctrl+F9.
To make this rebuild and reload even faster you can change your settings in `Settings -> Build, Execution, Deployment -> Gradle -> Build and run using: ` to build with Intellij instead of Gradle.

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