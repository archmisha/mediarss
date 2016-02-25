const serverPort = process.env.npm_package_config_Serverport || 8080;
console.log('serverPort: ' + serverPort);

module.exports = {
    entry: {
        home: "./app/App.js"
    },
    devtool: 'source-map',
    devServer: {
        proxy: {
            '/rest/*': {
                target: 'http://localhost:' + serverPort,
                secure: false
            }
        }
    },
    output: {
        filename: "public/[name].js"
    },
    module: {
        loaders: [
            {
                test: /\.jsx?$/,
                exclude: /(node_modules|bower_components)/,
                loaders: ["react-hot", 'babel?'+JSON.stringify(
                    {
                        presets: ['react', 'es2015'],
                        "plugins": [
                            "syntax-class-properties",
                            "syntax-decorators",
                            "syntax-object-rest-spread",

                            "transform-class-properties",
                            "transform-object-rest-spread"
                        ]
                    }
                )]
            },
            {
                test: /\.css$/,
                loader: 'style!css'
            },
            {
                test: /\.scss$/,
                loader: 'style!css!sass'
            },
            {
                test: /\.less$/,
                loader: 'style!css!less'
            }
        ]
    }
};