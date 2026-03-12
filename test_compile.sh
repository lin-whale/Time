#!/bin/bash
echo "检查 Kotlin 文件语法..."
for f in app/src/main/java/com/example/time/ui/components/*.kt; do
    echo "检查 $(basename $f)..."
    # 检查基本语法问题
    if grep -q "import.*\*$" "$f"; then
        echo "  警告: 发现通配符导入"
    fi
    # 检查未闭合的字符串
    if grep -P '"\s*$' "$f" | grep -v ".*\"$"; then
        echo "  错误: 可能有未闭合的字符串"
    fi
done
echo "语法检查完成"
