package src

import (
	"github.com/Unknwon/goconfig"
	"log"
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

// FileName 读取的配置文件名称
const FileName = "in-parameter.ini"

func init() {
	// 加载配置文件
	cfg, err := goconfig.LoadConfigFile(FileName)
	if err != nil {
		log.Fatal("配置文件[config.ini]不存在")
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
		log.Printf("%s读取失败：%s", key, err)
	}
	return value
}
