module.exports = {
    entry: {
        home: "./app/App.js"
    },
    devtool: 'source-map',
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