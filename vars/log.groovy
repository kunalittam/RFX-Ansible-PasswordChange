#!/usr/bin/env groovy

def info(message) {
    echo "\033[34mINFO: ${message}\033[0m"
}

def warning(message) {
    echo "\033[0;33mWARNING: ${message}\033[0m"
}

def error(message) {
    echo "\033[31mERROR: ${message}\033[0m"
}
