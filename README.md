
## 引入
    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

	dependencies {
        //compile 'com.github.User:Repo:Tag'
        compile 'com.github.githubRonda.ImageLoader:v0.1' // 这里也可以写成 imageloader
    }


## 权限

    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>

## 用法
    ImageLoader imageLoader = new ImageLoader();
    imageLoader.setImageCache(new DoubleCache());// 默认就是DoubleCache(内存缓存+磁盘缓存)
    // 除了DoubleCache外, 还有DiskCache, MemoryCache
    imageLoader.displayImage(iv_show, "https://www.baidu.com/img/bd_logo1.png");


