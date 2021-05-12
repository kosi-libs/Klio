// Workaround for webpack crash
// https://youtrack.jetbrains.com/issue/KT-46082
var webpack = require('webpack');

config.resolve.alias = {
    "buffer": require.resolve("buffer"),
    "crypto": false,
    "stream": require.resolve("stream-browserify"),
}

config.plugins.push(
    new webpack.ProvidePlugin({
        "Buffer": ['buffer', 'Buffer']
    })
)
