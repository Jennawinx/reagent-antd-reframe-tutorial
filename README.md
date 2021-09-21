
### Development mode
```
npm install
npx shadow-cljs watch app
```
recompile antd css (required only on version change)
```
lessc --js --clean-css src/less/antd.main.less > public/css/antd.css
```
start a ClojureScript REPL
```
npx shadow-cljs browser-repl
```
### Building for production

```
npx shadow-cljs release app
```
