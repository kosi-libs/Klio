// Workaround for webpack crash
// https://youtrack.jetbrains.com/issue/KT-46082
config.resolve.alias = {
    "buffer": false,
    "crypto": false,
    "stream": false,
}