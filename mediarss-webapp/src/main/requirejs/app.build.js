({
	appDir: "../webapp",
	baseUrl: "js",
	dir: "../../../target/require-web-1.0/scripts",
	//Comment out the optimize line if you want
	//the code minified by UglifyJS
//	optimize: "closure",

	name: "main",
	optimizeCss: 'none',
	mainConfigFile: '../webapp/js/main.js',
	wrapShim: true
})