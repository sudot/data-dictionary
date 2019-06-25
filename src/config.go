package src

import (
	"fmt"
	"github.com/Unknwon/goconfig"
	"os"
)

type Config struct {
	Host          string
	User          string
	Password      string
	Schema        string
	ExcludeTables string
	FilePath      string
}

var ConfigValue Config

const HomeSheetName = "总目录"

// 读取的配置文件名称
const FileName = "in-parameter.ini"

func init() {
	// 加载配置文件
	cfg, err := goconfig.LoadConfigFile(FileName)
	if err != nil {
		fmt.Println("配置文件不存在\r\n请按回车键继续")
		os.Stdin.Read(make([]byte, 1))
		os.Exit(-1)
	}
	ConfigValue = Config{
		Host:          getConfigValue(cfg, "host"),
		User:          getConfigValue(cfg, "user"),
		Password:      getConfigValue(cfg, "password"),
		Schema:        getConfigValue(cfg, "schema"),
		ExcludeTables: getConfigValue(cfg, "exclude-tables-string"),
		FilePath:      getConfigValue(cfg, "file-path"),
	}
}

func getConfigValue(cfg *goconfig.ConfigFile, key string) string {
	value, err := cfg.GetValue("", key)
	if err != nil {
		println("host读取失败", err)
	}
	return value
}
